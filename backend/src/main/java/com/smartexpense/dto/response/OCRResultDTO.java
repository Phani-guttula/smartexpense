package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OCRResultDTO {
    
    private String rawText;

    private BigDecimal amount;
    private LocalDate date;
    private String merchantName;
    
    private Double overallConfidence;
    private Double amountConfidence;
    private Double dateConfidence;
    
    private String suggestedCategory;
    
    private String notes;
}