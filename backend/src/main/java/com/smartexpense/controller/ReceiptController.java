package com.smartexpense.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartexpense.dto.response.ApiResponse;
import com.smartexpense.dto.response.OCRResultDTO;
import com.smartexpense.dto.response.ReceiptUploadResponse;
import com.smartexpense.service.FileUploadService;
import com.smartexpense.service.OCRService;


@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private OCRService ocrService;
    
    /**
     * Upload receipt and perform OCR
     * POST /api/v1/receipts/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ReceiptUploadResponse>> uploadReceipt(
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Step 1: Upload image to Cloudinary
            String receiptUrl = fileUploadService.uploadReceipt(file);
            
            // Step 2: Perform OCR on uploaded image
            OCRResultDTO ocrResult = ocrService.processReceipt(file);
            
            // Step 3: Determine if manual review is needed
            boolean requiresManualReview = ocrResult.getOverallConfidence() < 0.6;
            
            // Step 4: Prepare response
            ReceiptUploadResponse response = new ReceiptUploadResponse(
                receiptUrl,
                ocrResult,
                requiresManualReview
            );
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receipt uploaded and processed successfully", response));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process receipt: " + e.getMessage()));
        }
    }
    
    /**
     * Test OCR without uploading (for testing)
     * POST /api/v1/receipts/ocr-test
     */
    @PostMapping("/ocr-test")
    public ResponseEntity<ApiResponse<OCRResultDTO>> testOCR(
            @RequestParam("file") MultipartFile file) {
        
        try {
            OCRResultDTO ocrResult = ocrService.processReceipt(file);
            
            return ResponseEntity.ok(
                ApiResponse.success("OCR completed", ocrResult)
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("OCR failed: " + e.getMessage()));
        }
    }
}