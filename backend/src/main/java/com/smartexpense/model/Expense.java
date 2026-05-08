package com.smartexpense.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // Financial Details
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "category_code", nullable = false, length = 20)
    private String categoryCode;
    
    @Column(name = "sub_category", length = 100)
    private String subCategory;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Receipt Details
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    
    @Column(name = "merchant_name", length = 255)
    private String merchantName;
    
    // OCR & Storage
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;
    
    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    private String ocrRawText;
    
    @Column(name = "ocr_confidence", precision = 3, scale = 2)
    private BigDecimal ocrConfidence;
    
    // Verification
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_manually_entered")
    private Boolean isManuallyEntered = false;
    
    // Metadata
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isVerified == null) isVerified = false;
        if (isManuallyEntered == null) isManuallyEntered = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}