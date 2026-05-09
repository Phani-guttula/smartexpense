package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUploadResponse {
    
    private String receiptUrl; 
    private OCRResultDTO ocrResult;
    private Boolean requiresManualReview;
}