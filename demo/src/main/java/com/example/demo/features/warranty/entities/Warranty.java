package com.example.demo.features.warranty.entities;


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

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Table(name = "warranties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warranty {

	@Id
	@UuidGenerator
	@Column(name = "id", updatable = false, nullable = false, length = 36)
	private String id;

	@Size(max = 36)
	@Column(name = "user_id", length = 36)
	private String userId;

	@Size(max = 36)
	@Column(name = "customer_id", nullable = true, length = 36)
	private String customerId;

	@Size(max = 36)
	@Column(name = "order_id", nullable = true, length = 36)
	private String orderId;

	@Size(max = 36)
	@Column(name = "order_item_id", nullable = true, length = 36)
	private String orderItemId;

	@NotBlank(message = "Customer phone is required")
	@Size(max = 20, message = "Customer phone must not exceed 20 characters")
	@Column(name = "customer_phone", nullable = false, length = 20)
	private String customerPhone;

	@NotBlank(message = "Customer name is required")
	@Size(max = 100, message = "Customer name must not exceed 100 characters")
	@Column(name = "customer_name", nullable = false, length = 100)
	private String customerName;

	@NotBlank(message = "Issue description is required")
	@Size(max = 1000, message = "Issue description must not exceed 1000 characters")
	@Column(name = "issue_description", nullable = false, length = 1000)
	private String issueDescription;

	@NotNull(message = "Received date is required")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "received_date", nullable = false)
	private Date receivedDate;

	@NotNull(message = "Expected return date is required")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expected_return_date", nullable = false)
	private Date expectedReturnDate;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", updatable = false)
	private Date createdAt;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@NotNull(message = "Warranty status is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private WarrantyStatus status;

	@Size(max = 1000, message = "Technician note must not exceed 1000 characters")
	@Column(name = "technician_note", length = 1000)
	private String technicianNote;

	@Size(max = 1000, message = "Reject reason must not exceed 1000 characters")
	@Column(name = "reject_reason", length = 1000)
	private String rejectReason;

	@NotNull(message = "Quantity is required")
	@Min(value = 1, message = "Quantity must be at least 1")
	@Column(name = "quantity", nullable = false)
	@Builder.Default
	private Integer quantity = 1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "product_id", insertable = false, updatable = false, length = 36)
	private String productId;
}

