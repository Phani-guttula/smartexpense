package com.smartexpense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    
    private Long id;
    private BigDecimal amount;
    private String categoryCode;
    private String categoryName;
    private String subCategory;
    private String description;
    private LocalDate expenseDate;
    private String merchantName;
    private String receiptUrl;
    private Boolean isVerified;
    private Boolean isManuallyEntered;
    private LocalDateTime createdAt;
}