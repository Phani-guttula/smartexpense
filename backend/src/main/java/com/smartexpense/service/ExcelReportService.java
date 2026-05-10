package com.smartexpense.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartexpense.dto.response.ITRReportDTO;
import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import com.smartexpense.model.User;
import com.smartexpense.repository.CategoryRepository;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.repository.UserRepository;

@Service
public class ExcelReportService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Date formatter for display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
   
    public ByteArrayInputStream generateITRReport(String financialYear) throws IOException {
        
        // Get current user
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate[] dateRange = parseFinancialYear(financialYear);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        
        // Fetch expenses for the financial year
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // Prepare report data
        ITRReportDTO reportData = prepareReportData(user, financialYear, expenses);
        
        // Generate Excel workbook
        XSSFWorkbook workbook = createWorkbook(reportData);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        
        return new ByteArrayInputStream(out.toByteArray());
    }

    private XSSFWorkbook createWorkbook(ITRReportDTO reportData) {
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Create styles
        Map<String, CellStyle> styles = createStyles(workbook);
        
        // Sheet 1: Summary
        createSummarySheet(workbook, reportData, styles);
        
        // Sheet 2: Category-wise Breakdown
        createCategoryBreakdownSheet(workbook, reportData, styles);
        
        // Sheet 3: Detailed Transactions
        createTransactionSheet(workbook, reportData, styles);
        
        // Sheet 4: Instructions
        createInstructionsSheet(workbook, reportData, styles);
        
        return workbook;
    }
    
    private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        
        Map<String, CellStyle> styles = new HashMap<>();
        
        // Title style
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)79, (byte)70, (byte)229}, null)); // Primary color
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("title", titleStyle);
        
        // Header style
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)67, (byte)56, (byte)202}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("header", headerStyle);
        
        // Subheader style
        XSSFCellStyle subheaderStyle = workbook.createCellStyle();
        XSSFFont subheaderFont = workbook.createFont();
        subheaderFont.setBold(true);
        subheaderFont.setFontHeightInPoints((short) 11);
        subheaderStyle.setFont(subheaderFont);
        subheaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        subheaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("subheader", subheaderStyle);
        
        // Data style
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(new byte[]{(byte)220, (byte)220, (byte)220}, null));
        styles.put("data", dataStyle);
        
        // Currency style
        XSSFCellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));
        styles.put("currency", currencyStyle);
        
        // Date style
        XSSFCellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataStyle);
        dateStyle.setDataFormat(format.getFormat("dd-mmm-yyyy"));
        styles.put("date", dateStyle);
        
        // Success style (green)
        XSSFCellStyle successStyle = workbook.createCellStyle();
        successStyle.cloneStyleFrom(dataStyle);
        XSSFFont successFont = workbook.createFont();
        successFont.setColor(IndexedColors.DARK_GREEN.getIndex());
        successFont.setBold(true);
        successStyle.setFont(successFont);
        styles.put("success", successStyle);
        
        // Warning style (orange)
        XSSFCellStyle warningStyle = workbook.createCellStyle();
        warningStyle.cloneStyleFrom(dataStyle);
        XSSFFont warningFont = workbook.createFont();
        warningFont.setColor(IndexedColors.DARK_YELLOW.getIndex());
        warningFont.setBold(true);
        warningStyle.setFont(warningFont);
        styles.put("warning", warningStyle);
        
        return styles;
    }
    
   
     // Sheet 1: Summary Sheet  
    private void createSummarySheet(XSSFWorkbook workbook, ITRReportDTO reportData, Map<String, CellStyle> styles) {
        
        XSSFSheet sheet = workbook.createSheet("Summary");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ITR Report - Financial Year " + reportData.getFinancialYear());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum++; // Empty row
        
        // User information
        createInfoRow(sheet, rowNum++, "Generated For:", reportData.getUserName(), styles);
        createInfoRow(sheet, rowNum++, "Email:", reportData.getUserEmail(), styles);
        createInfoRow(sheet, rowNum++, "Report Date:", reportData.getGeneratedDate(), styles);
        
        rowNum++; // Empty row
        
        // Summary section header
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("FINANCIAL SUMMARY");
        summaryHeaderCell.setCellStyle(styles.get("subheader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));
        
        // Summary data
        createSummaryRow(sheet, rowNum++, "Total Expenses:", reportData.getTotalExpenses(), styles);
        createSummaryRow(sheet, rowNum++, "Total Tax Deductions:", reportData.getTotalDeductions(), styles);
        createSummaryRow(sheet, rowNum++, "Estimated Tax Saving (20% bracket):", reportData.getEstimatedTaxSaving(), styles);
        
        rowNum++; // Empty row
        
        // Category summary header
        Row catHeaderRow = sheet.createRow(rowNum++);
        catHeaderRow.setHeightInPoints(25);
        String[] headers = {"ITR Section", "Category", "Total Amount", "Max Limit", "Eligible Amount", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = catHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Category data
        for (ITRReportDTO.CategorySummary summary : reportData.getCategorySummaries()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(summary.getItrSection() != null ? summary.getItrSection() : "N/A");
            row.createCell(1).setCellValue(summary.getCategoryName());
            
            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(summary.getTotalAmount().doubleValue());
            amountCell.setCellStyle(styles.get("currency"));
            
            Cell limitCell = row.createCell(3);
            if (summary.getMaxLimit() != null) {
                limitCell.setCellValue(summary.getMaxLimit().doubleValue());
                limitCell.setCellStyle(styles.get("currency"));
            } else {
                limitCell.setCellValue("No Limit");
            }
            
            Cell eligibleCell = row.createCell(4);
            eligibleCell.setCellValue(summary.getEligibleAmount().doubleValue());
            eligibleCell.setCellStyle(styles.get("currency"));
            
            Cell statusCell = row.createCell(5);
            statusCell.setCellValue(summary.getStatus());
            statusCell.setCellStyle(
                summary.getStatus().equals("Exceeded Limit") ? styles.get("warning") : styles.get("success")
            );
            
            // Apply borders to all cells
            for (int i = 0; i < 6; i++) {
                if (row.getCell(i) != null && row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(styles.get("data"));
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000); // Add padding
        }
    }
    
     //Sheet 2: Category Breakdown
     
    private void createCategoryBreakdownSheet(XSSFWorkbook workbook, ITRReportDTO reportData, Map<String, CellStyle> styles) {
        
        XSSFSheet sheet = workbook.createSheet("Category Breakdown");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Category-wise Expense Breakdown");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        
        rowNum++; // Empty row
        
        // Group expenses by category
        Map<String, List<ITRReportDTO.ExpenseDetail>> groupedExpenses = reportData.getExpenseDetails()
                .stream()
                .collect(Collectors.groupingBy(ITRReportDTO.ExpenseDetail::getCategory));
        
        // For each category
        for (Map.Entry<String, List<ITRReportDTO.ExpenseDetail>> entry : groupedExpenses.entrySet()) {
            
            String category = entry.getKey();
            List<ITRReportDTO.ExpenseDetail> expenses = entry.getValue();
            
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            Cell catCell = catRow.createCell(0);
            catCell.setCellValue(category + " (" + expenses.size() + " transactions)");
            catCell.setCellStyle(styles.get("subheader"));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
            
            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Date", "Merchant", "Sub Category", "Description", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styles.get("header"));
            }
            
            // Expense details
            BigDecimal categoryTotal = BigDecimal.ZERO;
            for (ITRReportDTO.ExpenseDetail expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(expense.getDate());
                row.createCell(1).setCellValue(expense.getMerchantName() != null ? expense.getMerchantName() : "");
                row.createCell(2).setCellValue(expense.getSubCategory() != null ? expense.getSubCategory() : "");
                row.createCell(3).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
                
                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(expense.getAmount().doubleValue());
                amountCell.setCellStyle(styles.get("currency"));
                
                categoryTotal = categoryTotal.add(expense.getAmount());
                
                // Apply borders
                for (int i = 0; i < 5; i++) {
                    if (row.getCell(i).getCellStyle() == null) {
                        row.getCell(i).setCellStyle(styles.get("data"));
                    }
                }
            }
            
            // Category total
            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("Category Total:");
            totalLabelCell.setCellStyle(styles.get("subheader"));
            
            Cell totalAmountCell = totalRow.createCell(4);
            totalAmountCell.setCellValue(categoryTotal.doubleValue());
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(styles.get("currency"));
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalStyle.setFont(boldFont);
            totalAmountCell.setCellStyle(totalStyle);
            
            rowNum++; // Empty row between categories
        }
        
        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }
    

     // Sheet 3: All Transactions
    private void createTransactionSheet(XSSFWorkbook workbook, ITRReportDTO reportData, Map<String, CellStyle> styles) {
        
        XSSFSheet sheet = workbook.createSheet("All Transactions");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Complete Transaction List");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(20);
        String[] headers = {"Date", "Category", "Merchant", "Sub Category", "Description", "Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Data rows
        for (ITRReportDTO.ExpenseDetail expense : reportData.getExpenseDetails()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(expense.getDate());
            row.createCell(1).setCellValue(expense.getCategory());
            row.createCell(2).setCellValue(expense.getMerchantName() != null ? expense.getMerchantName() : "");
            row.createCell(3).setCellValue(expense.getSubCategory() != null ? expense.getSubCategory() : "");
            row.createCell(4).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
            
            Cell amountCell = row.createCell(5);
            amountCell.setCellValue(expense.getAmount().doubleValue());
            amountCell.setCellStyle(styles.get("currency"));
            
            // Apply borders
            for (int i = 0; i < 6; i++) {
                if (row.getCell(i).getCellStyle() == null) {
                    row.getCell(i).setCellStyle(styles.get("data"));
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }
    
     // Sheet 4: Instructions 
    private void createInstructionsSheet(XSSFWorkbook workbook, ITRReportDTO reportData, Map<String, CellStyle> styles) {
        
        XSSFSheet sheet = workbook.createSheet("Instructions");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("How to Use This Report for ITR Filing");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        
        rowNum++; // Empty row
        
        // Instructions
        String[] instructions = {
            "STEP 1: Review the Summary Sheet",
            "- Check total deductions and estimated tax savings",
            "- Verify that amounts are within prescribed limits",
            "",
            "STEP 2: Verify Category Breakdown",
            "- Review each category's expenses",
            "- Ensure all transactions are correctly categorized",
            "- Keep receipts for audit purposes",
            "",
            "STEP 3: Filing Your ITR",
            "Section 80C (Max ₹1,50,000):",
            "  - LIC premiums, PPF, ELSS, NSC, Home loan principal",
            "  - Enter total in ITR form under 'Deductions - Chapter VIA'",
            "",
            "Section 80D (Max ₹25,000 for self, ₹50,000 for parents):",
            "  - Health insurance premiums",
            "  - Medical expenses for senior citizens",
            "",
            "Section 24(b) (Max ₹2,00,000):",
            "  - Home loan interest for self-occupied property",
            "",
            "HRA (Section 10(13A)):",
            "  - Least of: Actual HRA, Rent paid minus 10% of salary, 50% of salary (metro)",
            "",
            "STEP 4: Important Notes",
            "- This is a generated report for reference only",
            "- Consult a tax professional for accurate filing",
            "- Keep all original receipts for 6 years",
            "- SmartExpense is not responsible for tax calculation errors",
            "",
            "STEP 5: Contact Support",
            "- For questions: support@smartexpense.com",
            "- Generated by SmartExpense - Expense Tracking Made Easy"
        };
        
        for (String instruction : instructions) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            
            if (instruction.startsWith("STEP")) {
                cell.setCellStyle(styles.get("subheader"));
            }
            
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
        }
        
        // Auto-size column
        sheet.setColumnWidth(0, 15000);
    }
    
    // Helper methods
    
    private void createInfoRow(XSSFSheet sheet, int rowNum, String label, String value, Map<String, CellStyle> styles) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }
    
    private void createSummaryRow(XSSFSheet sheet, int rowNum, String label, BigDecimal value, Map<String, CellStyle> styles) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.get("subheader"));
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(styles.get("currency"));
    }
    
    // Prepare report data from expenses
    private ITRReportDTO prepareReportData(User user, String financialYear, List<Expense> expenses) {
        
        ITRReportDTO report = new ITRReportDTO();
        
        // Basic info
        report.setFinancialYear(financialYear);
        report.setUserName(user.getFullName());
        report.setUserEmail(user.getEmail());
        report.setGeneratedDate(LocalDate.now().format(DATE_FORMATTER));
        
        // Calculate totals
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalExpenses(totalExpenses);
        
        // Group by category
        Map<String, List<Expense>> categoryGroups = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategoryCode));
        
        List<ITRReportDTO.CategorySummary> categorySummaries = new ArrayList<>();
        BigDecimal totalDeductions = BigDecimal.ZERO;
        
        for (Map.Entry<String, List<Expense>> entry : categoryGroups.entrySet()) {
            String categoryCode = entry.getKey();
            List<Expense> categoryExpenses = entry.getValue();
            
            Category category = categoryRepository.findByCode(categoryCode).orElse(null);
            
            BigDecimal categoryTotal = categoryExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal maxLimit = category != null ? category.getMaxLimit() : null;
            BigDecimal eligibleAmount = maxLimit != null 
                    ? categoryTotal.min(maxLimit) 
                    : categoryTotal;
            
            String status = maxLimit == null ? "No Limit" 
                    : categoryTotal.compareTo(maxLimit) > 0 ? "Exceeded Limit" 
                    : "Within Limit";
            
            ITRReportDTO.CategorySummary summary = new ITRReportDTO.CategorySummary(
                categoryCode,
                category != null ? category.getName() : categoryCode,
                category != null ? category.getItrSection() : null,
                categoryTotal,
                maxLimit,
                eligibleAmount,
                categoryExpenses.size(),
                status
            );
            
            categorySummaries.add(summary);
            totalDeductions = totalDeductions.add(eligibleAmount);
        }
        
        report.setCategorySummaries(categorySummaries);
        report.setTotalDeductions(totalDeductions);
        
        // Estimated tax saving (20% of deductions)
        BigDecimal taxSaving = totalDeductions.multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);
        report.setEstimatedTaxSaving(taxSaving);
        
        // Expense details
        List<ITRReportDTO.ExpenseDetail> expenseDetails = expenses.stream()
                .sorted(Comparator.comparing(Expense::getExpenseDate).reversed())
                .map(exp -> new ITRReportDTO.ExpenseDetail(
                    exp.getExpenseDate().format(DATE_FORMATTER),
                    exp.getMerchantName(),
                    exp.getCategory() != null ? exp.getCategory().getName() : exp.getCategoryCode(),
                    exp.getSubCategory(),
                    exp.getDescription(),
                    exp.getAmount(),
                    exp.getReceiptUrl()
                ))
                .collect(Collectors.toList());
        
        report.setExpenseDetails(expenseDetails);
        
        return report;
    }
    
     // Parse financial year string to date range 
    private LocalDate[] parseFinancialYear(String financialYear) {
        // Format: "2024-25" means April 1, 2024 to March 31, 2025
        String[] parts = financialYear.split("-");
        int startYear = Integer.parseInt("20" + parts[0]);
        int endYear = Integer.parseInt("20" + parts[1]);
        
        LocalDate startDate = LocalDate.of(startYear, 4, 1);  // April 1st
        LocalDate endDate = LocalDate.of(endYear, 3, 31);    // March 31st
        
        return new LocalDate[]{startDate, endDate};
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}