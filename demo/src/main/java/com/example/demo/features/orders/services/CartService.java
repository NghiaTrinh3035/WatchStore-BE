package com.example.demo.features.orders.services;


import com.example.demo.features.communications.controllers.*;
import com.example.demo.features.warranty.dtos.request.*;
import com.example.demo.features.communications.entities.*;
import com.example.demo.features.auth.dtos.response.*;
import com.example.demo.features.inventory.services.*;
import com.example.demo.features.orders.dtos.request.*;
import com.example.demo.features.auth.dtos.request.*;
import com.example.demo.features.reports.dtos.response.*;
import com.example.demo.features.reports.services.*;
import com.example.demo.features.orders.entities.*;
import com.example.demo.features.inventory.controllers.*;
import com.example.demo.features.vouchers.dtos.response.*;
import com.example.demo.features.communications.repositories.*;
import com.example.demo.features.inventory.dtos.request.*;
import com.example.demo.core.enums.*;
import com.example.demo.core.dtos.response.*;
import com.example.demo.features.auth.controllers.*;
import com.example.demo.features.reports.repositories.*;
import com.example.demo.core.services.*;
import com.example.demo.features.vouchers.controllers.*;
import com.example.demo.features.warranty.services.*;
import com.example.demo.features.communications.dtos.response.*;
import com.example.demo.features.orders.dtos.response.*;
import com.example.demo.core.exceptions.*;
import com.example.demo.features.reports.dtos.request.*;
import com.example.demo.features.auth.services.*;
import com.example.demo.features.users.dtos.response.*;
import com.example.demo.features.users.services.*;
import com.example.demo.features.users.controllers.*;
import com.example.demo.features.products.dtos.response.*;
import com.example.demo.core.config.*;
import com.example.demo.features.orders.services.payment.*;
import com.example.demo.features.vouchers.dtos.request.*;
import com.example.demo.features.products.services.*;
import com.example.demo.features.vouchers.services.*;
import com.example.demo.core.entities.*;
import com.example.demo.features.warranty.entities.*;
import com.example.demo.features.inventory.dtos.response.*;
import com.example.demo.features.warranty.controllers.*;
import com.example.demo.features.users.entities.*;
import com.example.demo.features.products.dtos.request.*;
import com.example.demo.features.warranty.repositories.*;
import com.example.demo.features.inventory.repositories.*;
import com.example.demo.features.communications.dtos.request.*;
import com.example.demo.features.warranty.dtos.response.*;
import com.example.demo.features.orders.controllers.*;
import com.example.demo.features.products.entities.*;
import com.example.demo.features.vouchers.entities.*;
import com.example.demo.features.products.controllers.*;
import com.example.demo.features.reports.controllers.*;
import com.example.demo.features.inventory.entities.*;
import com.example.demo.features.communications.services.*;
import com.example.demo.features.orders.services.*;
import com.example.demo.features.users.dtos.request.*;
import com.example.demo.features.reports.entities.*;
import com.example.demo.core.common.*;
import com.example.demo.features.products.repositories.*;
import com.example.demo.features.orders.repositories.*;
import com.example.demo.features.users.repositories.*;
import com.example.demo.features.vouchers.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public CartResponse getOrCreateCart(String customerId) {
        return toResponse(getOrCreateCartEntity(customerId));
    }

    @Transactional(readOnly = true)
    public CartResponse getOrCreateCartResponse(String customerId) {
        return getOrCreateCart(customerId);
    }

    private Cart getOrCreateCartEntity(String customerId) {
        accessControlService.requireCustomerAccess(customerId);
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
            Cart cart = Cart.builder().customer(customer).build();
            return cartRepository.save(cart);
        });
    }

    public CartResponse addItem(String customerId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getOrCreateCartEntity(customerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        int availableStock = stockOf(product);
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int current = item.getQuantity() == null ? 0 : item.getQuantity();
                            int nextQuantity = current + quantity;
                            long price = item.getProduct().getPrice() == null ? 0L : item.getProduct().getPrice();
                            item.setQuantity(nextQuantity);
                            item.setSubTotal(price * nextQuantity);
                        },
                        () -> cart.getItems().add(
                                CartItem.builder()
                                        .cart(cart)
                                        .product(product)
                                        .quantity(quantity)
                                        .subTotal((product.getPrice() == null ? 0L : product.getPrice()) * quantity)
                                        .build()
                        )
                );

        product.setStockQuantity(availableStock - quantity);
        recalcTotal(cart);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse addItemResponse(String customerId, String productId, int quantity) {
        return addItem(customerId, productId, quantity);
    }

    public CartResponse updateItemQuantity(String customerId, String productId, int quantity) {
        Cart cart = getOrCreateCartEntity(customerId);
        if (quantity <= 0) {
            return removeItem(customerId, productId);
        }

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct() != null && cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại trong giỏ hàng: " + productId));

        Product product = item.getProduct();
        if (product == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        int currentQuantity = item.getQuantity() == null ? 0 : item.getQuantity();
        int delta = quantity - currentQuantity;
        int availableStock = stockOf(product);

        if (delta > 0) {
            if (delta > availableStock) {
                throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
            }
            product.setStockQuantity(availableStock - delta);
        } else if (delta < 0) {
            product.setStockQuantity(availableStock + Math.abs(delta));
        }

        long price = product.getPrice() == null ? 0L : product.getPrice();
        item.setQuantity(quantity);
        item.setSubTotal(price * quantity);

        recalcTotal(cart);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse updateItemQuantityResponse(String customerId, String productId, int quantity) {
        return updateItemQuantity(customerId, productId, quantity);
    }

    public CartResponse removeItem(String customerId, String productId) {
        Cart cart = getOrCreateCartEntity(customerId);
        CartItem target = cart.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại trong giỏ hàng: " + productId));

        Product product = target.getProduct();
        int quantity = target.getQuantity() == null ? 0 : target.getQuantity();
        if (product != null) {
            product.setStockQuantity(stockOf(product) + quantity);
        }

        cart.getItems().remove(target);
        recalcTotal(cart);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse removeItemResponse(String customerId, String productId) {
        return removeItem(customerId, productId);
    }

    public void clearCart(String customerId) {
        clearCart(customerId, true);
    }

    public void clearCart(String customerId, boolean restock) {
        Cart cart = getOrCreateCartEntity(customerId);
        if (restock) {
            cart.getItems().forEach(item -> {
                Product product = item.getProduct();
                int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                if (product != null) {
                    product.setStockQuantity(stockOf(product) + quantity);
                }
            });
        }
        cart.getItems().clear();
        cart.setTotalAmount(0L);
        cartRepository.save(cart);
    }

    private int stockOf(Product product) {
        if (product == null || product.getStockQuantity() == null) {
            return 0;
        }
        return Math.max(product.getStockQuantity(), 0);
    }

    private void recalcTotal(Cart cart) {
        long total = cart.getItems().stream()
                .mapToLong(item -> {
                    long price = item.getProduct() == null || item.getProduct().getPrice() == null ? 0L : item.getProduct().getPrice();
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    return price * quantity;
                })
                .sum();
        cart.setTotalAmount(total);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return CartResponse.builder()
                .id(cart.getId())
                .totalAmount(cart.getTotalAmount() == null ? 0L : cart.getTotalAmount())
                .customerId(cart.getCustomer() == null ? null : cart.getCustomer().getId())
                .items(items)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        long unitPrice = item.getProduct() == null || item.getProduct().getPrice() == null ? 0L : item.getProduct().getPrice();
        long subTotal = item.getSubTotal() == null ? unitPrice * quantity : item.getSubTotal();

        return CartItemResponse.builder()
                .id(item.getId())
                .quantity(quantity)
                .subTotal(subTotal)
                .product(toProductResponse(item.getProduct()))
                .build();
    }

    private CartProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }
        return CartProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
}
