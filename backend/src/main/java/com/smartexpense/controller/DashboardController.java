package com.smartexpense.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.DashboardDTO;
import com.smartexpense.dto.response.ApiResponse;
import com.smartexpense.service.ExpenseService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard() {
        DashboardDTO dashboard = expenseService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}