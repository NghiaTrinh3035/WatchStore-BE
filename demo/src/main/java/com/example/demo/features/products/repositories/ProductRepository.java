package com.example.demo.features.products.repositories;


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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE p.category.id = :categoryId OR c.id = :categoryId")
    List<Product> findByAnyCategoryId(@Param("categoryId") String categoryId);

    @Query("SELECT CASE WHEN COUNT(DISTINCT p) > 0 THEN true ELSE false END FROM Product p LEFT JOIN p.categories c WHERE p.category.id = :categoryId OR c.id = :categoryId")
    boolean existsByAnyCategoryId(@Param("categoryId") String categoryId);

    List<Product> findByStatus(ProductStatus status);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) " +
            "AND (:color IS NULL OR LOWER(COALESCE(p.wireColor, '')) LIKE LOWER(CONCAT('%', :color, '%')) " +
            "OR LOWER(COALESCE(p.caseColor, '')) LIKE LOWER(CONCAT('%', :color, '%')) " +
            "OR LOWER(COALESCE(p.faceColor, '')) LIKE LOWER(CONCAT('%', :color, '%'))) " +
            "AND (:faceSize IS NULL OR LOWER(COALESCE(p.faceSize, '')) LIKE LOWER(CONCAT('%', :faceSize, '%'))) " +
            "AND (:spec IS NULL OR LOWER(COALESCE(p.movementType, '')) LIKE LOWER(CONCAT('%', :spec, '%')) " +
            "OR LOWER(COALESCE(p.glassMaterial, '')) LIKE LOWER(CONCAT('%', :spec, '%')) " +
            "OR LOWER(COALESCE(p.waterResistance, '')) LIKE LOWER(CONCAT('%', :spec, '%')) " +
            "OR LOWER(COALESCE(p.wireMaterial, '')) LIKE LOWER(CONCAT('%', :spec, '%')) " +
            "OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :spec, '%'))) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(@Param("name") String name,
                                 @Param("brand") String brand,
                                 @Param("color") String color,
                                 @Param("faceSize") String faceSize,
                                 @Param("spec") String spec,
                                 @Param("status") ProductStatus status,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.status = 'ACTIVE'")
    List<Product> findAvailableProducts();

    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.status = :status")
    List<Product> findByBrandAndStatus(@Param("brand") String brand, @Param("status") ProductStatus status);

    @Query("SELECT CASE WHEN COUNT(DISTINCT p) > 0 THEN true ELSE false END FROM Product p LEFT JOIN p.categories c WHERE LOWER(p.name) = LOWER(:name) AND (p.category.id = :categoryId OR c.id = :categoryId)")
    boolean existsByNameAndAnyCategoryId(@Param("name") String name, @Param("categoryId") String categoryId);

    @Query("SELECT CASE WHEN COUNT(DISTINCT p) > 0 THEN true ELSE false END FROM Product p LEFT JOIN p.categories c WHERE LOWER(p.name) = LOWER(:name) AND (p.category.id = :categoryId OR c.id = :categoryId) AND p.id <> :id")
    boolean existsByNameAndAnyCategoryIdAndIdNot(@Param("name") String name, @Param("categoryId") String categoryId, @Param("id") String id);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.product.id = :productId")
    boolean existsRelatedTransactions(@Param("productId") String productId);
}
