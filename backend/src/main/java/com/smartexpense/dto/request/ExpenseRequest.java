package com.smartexpense.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;
    
    @NotBlank(message = "Category is required")
    private String categoryCode;
    
    private String subCategory;
    
    private String description;
    
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
    
    private String merchantName;
    
    private String receiptUrl;
    
    private Boolean isManuallyEntered = true;
}