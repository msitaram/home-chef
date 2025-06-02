package com.foodmarketplace.payment.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    public UUID orderId;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    public UUID customerId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @NotBlank
    @Column(name = "currency", nullable = false)
    public String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    public PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_transaction_id")
    public String gatewayTransactionId;

    @Column(name = "gateway_response", length = 2000)
    public String gatewayResponse;

    @Column(name = "failure_reason", length = 500)
    public String failureReason;

    @Column(name = "reserved_at")
    public LocalDateTime reservedAt;

    @Column(name = "captured_at")
    public LocalDateTime capturedAt;

    @Column(name = "refunded_at")
    public LocalDateTime refundedAt;

    @Column(name = "failed_at")
    public LocalDateTime failedAt;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Compensation tracking
    @Column(name = "compensation_status")
    @Enumerated(EnumType.STRING)
    public CompensationStatus compensationStatus = CompensationStatus.NONE;

    @Column(name = "compensation_attempts")
    public Integer compensationAttempts = 0;

    @Column(name = "compensation_reason", length = 500)
    public String compensationReason;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Static finder methods
    public static PaymentTransaction findByOrderId(UUID orderId) {
        return find("orderId", orderId).firstResult();
    }

    public static List<PaymentTransaction> findByCustomerId(UUID customerId) {
        return find("customerId", customerId).list();
    }

    public static List<PaymentTransaction> findByStatus(PaymentStatus status) {
        return find("status", status).list();
    }

    public static List<PaymentTransaction> findPendingCompensations() {
        return find("compensationStatus = ?1 and compensationAttempts < 3", 
                   CompensationStatus.PENDING).list();
    }

    public static List<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId) {
        return find("gatewayTransactionId", gatewayTransactionId).list();
    }

    public enum PaymentMethod {
        UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING, WALLET, COD
    }

    public enum PaymentStatus {
        PENDING,           // Payment initiated
        RESERVED,          // Amount reserved (not captured)
        CAPTURED,          // Amount captured/confirmed
        FAILED,            // Payment failed
        REFUNDED,          // Payment refunded
        PARTIALLY_REFUNDED // Partial refund processed
    }

    public enum CompensationStatus {
        NONE,              // No compensation needed
        PENDING,           // Compensation required
        IN_PROGRESS,       // Compensation being processed
        COMPLETED,         // Compensation completed
        FAILED             // Compensation failed (manual intervention required)
    }
}
