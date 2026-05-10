package com.smartexpense.controller;

import com.smartexpense.service.ExcelReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    
    @Autowired
    private ExcelReportService excelReportService;
    
     // Download ITR report as Excel file
    @GetMapping("/itr")
    public ResponseEntity<InputStreamResource> downloadITRReport(
            @RequestParam(defaultValue = "2024-25") String financialYear) {
        
        try {
            ByteArrayInputStream excelStream = excelReportService.generateITRReport(financialYear);
            
            // Generate filename with current date
            String filename = String.format("ITR_Report_FY%s_%s.xlsx", 
                    financialYear, 
                    LocalDate.now().toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelStream));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    //  Get available financial years for dropdown  
    @GetMapping("/financial-years")
    public ResponseEntity<String[]> getAvailableFinancialYears() {
        
        // Generate last 5 financial years
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        
        // If current month is Jan-Mar, we're in previous FY
        int fyStartYear = currentMonth < 4 ? currentYear - 1 : currentYear;
        
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            int year = fyStartYear - i;
            years[i] = String.format("%02d-%02d", year % 100, (year + 1) % 100);
        }
        
        return ResponseEntity.ok(years);
    }
}