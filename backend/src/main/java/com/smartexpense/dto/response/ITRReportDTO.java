package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ITRReportDTO {
    
    private String financialYear;
    private String userName;
    private String userEmail;
    private String generatedDate;
    
    // Summary data
    private BigDecimal totalExpenses;
    private BigDecimal totalDeductions;
    private BigDecimal estimatedTaxSaving;
    
    // Category-wise breakdown
    private List<CategorySummary> categorySummaries;
    
    // Detailed transactions
    private List<ExpenseDetail> expenseDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryCode;
        private String categoryName;
        private String itrSection;
        private BigDecimal totalAmount;
        private BigDecimal maxLimit;
        private BigDecimal eligibleAmount;  // Min of totalAmount and maxLimit
        private Integer transactionCount;
        private String status;  // "Within Limit", "Exceeded Limit", "No Limit"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseDetail {
        private String date;
        private String merchantName;
        private String category;
        private String subCategory;
        private String description;
        private BigDecimal amount;
        private String receiptUrl;
    }
}