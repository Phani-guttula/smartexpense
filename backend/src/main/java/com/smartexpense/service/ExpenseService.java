package com.smartexpense.service;

import com.smartexpense.dto.DashboardDTO;
import com.smartexpense.dto.ExpenseRequest;
import com.smartexpense.dto.ExpenseResponse;
import com.smartexpense.exception.BadRequestException;
import com.smartexpense.exception.ResourceNotFoundException;
import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import com.smartexpense.model.User;
import com.smartexpense.repository.CategoryRepository;
import com.smartexpense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Get current authenticated user ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
    
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Long userId = getCurrentUserId();
        
        // Validate category exists
        if (!categoryRepository.existsByCode(request.getCategoryCode())) {
            throw new BadRequestException("Invalid category code: " + request.getCategoryCode());
        }
        
        // Create expense
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setAmount(request.getAmount());
        expense.setCategoryCode(request.getCategoryCode());
        expense.setSubCategory(request.getSubCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setMerchantName(request.getMerchantName());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setIsManuallyEntered(request.getIsManuallyEntered());
        expense.setIsVerified(true); // Manually entered expenses are verified
        
        Expense savedExpense = expenseRepository.save(expense);
        
        return convertToResponse(savedExpense);
    }
    
    public Page<ExpenseResponse> getExpenses(String categoryCode, LocalDate startDate, 
                                            LocalDate endDate, int page, int size) {
        Long userId = getCurrentUserId();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("expenseDate").descending());
        
        Page<Expense> expenses;
        
        if (categoryCode != null && !categoryCode.isEmpty()) {
            expenses = expenseRepository.findByUserIdAndCategoryCode(userId, categoryCode, pageable);
        } else if (startDate != null && endDate != null) {
            List<Expense> filteredExpenses = expenseRepository.findByUserIdAndDateRange(
                    userId, startDate, endDate);
            // Convert list to page manually (simplified)
            expenses = expenseRepository.findByUserId(userId, pageable);
        } else {
            expenses = expenseRepository.findByUserId(userId, pageable);
        }
        
        return expenses.map(this::convertToResponse);
    }
    
    public ExpenseResponse getExpenseById(Long id) {
        Long userId = getCurrentUserId();
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        
        // Verify expense belongs to current user
        if (!expense.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to expense");
        }
        
        return convertToResponse(expense);
    }
    
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long userId = getCurrentUserId();
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        
        // Verify expense belongs to current user
        if (!expense.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to expense");
        }
        
        // Validate category if changed
        if (!expense.getCategoryCode().equals(request.getCategoryCode())) {
            if (!categoryRepository.existsByCode(request.getCategoryCode())) {
                throw new BadRequestException("Invalid category code: " + request.getCategoryCode());
            }
        }
        
        // Update fields
        expense.setAmount(request.getAmount());
        expense.setCategoryCode(request.getCategoryCode());
        expense.setSubCategory(request.getSubCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setMerchantName(request.getMerchantName());
        
        Expense updatedExpense = expenseRepository.save(expense);
        
        return convertToResponse(updatedExpense);
    }
    
    @Transactional
    public void deleteExpense(Long id) {
        Long userId = getCurrentUserId();
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        
        // Verify expense belongs to current user
        if (!expense.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to expense");
        }
        
        expenseRepository.delete(expense);
    }
    
    public DashboardDTO getDashboardData() {
        Long userId = getCurrentUserId();
        
        DashboardDTO dashboard = new DashboardDTO();
        
        // Total expenses
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);
        dashboard.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        
        // Estimated tax saving (20% of deductible expenses)
        BigDecimal taxSaving = (totalExpenses != null ? totalExpenses : BigDecimal.ZERO)
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);
        dashboard.setEstimatedTaxSaving(taxSaving);
        
        // Receipt count
        Long receiptCount = expenseRepository.countByUserId(userId);
        dashboard.setReceiptCount(receiptCount);
        
        // Category-wise breakdown
        List<Object[]> categoryData = expenseRepository.getCategoryWiseBreakdown(userId);
        List<DashboardDTO.CategoryBreakdown> breakdowns = new ArrayList<>();
        
        for (Object[] row : categoryData) {
            String categoryCode = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            Category category = categoryRepository.findByCode(categoryCode).orElse(null);
            String categoryName = category != null ? category.getName() : categoryCode;
            
            Double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0 
                    ? amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue()
                    : 0.0;
            
            breakdowns.add(new DashboardDTO.CategoryBreakdown(
                    categoryCode, categoryName, amount, percentage));
        }
        dashboard.setCategoryBreakdown(breakdowns);
        
        // Monthly trend (current year)
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlyData = expenseRepository.getMonthlyExpenses(userId, currentYear);
        List<DashboardDTO.MonthlyTrend> trends = new ArrayList<>();
        
        for (Object[] row : monthlyData) {
            Integer month = (Integer) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            String monthName = java.time.Month.of(month)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            trends.add(new DashboardDTO.MonthlyTrend(monthName, amount));
        }
        dashboard.setMonthlyTrend(trends);
        
        return dashboard;
    }
    
    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setCategoryCode(expense.getCategoryCode());
        response.setCategoryName(expense.getCategory() != null ? 
                expense.getCategory().getName() : expense.getCategoryCode());
        response.setSubCategory(expense.getSubCategory());
        response.setDescription(expense.getDescription());
        response.setExpenseDate(expense.getExpenseDate());
        response.setMerchantName(expense.getMerchantName());
        response.setReceiptUrl(expense.getReceiptUrl());
        response.setIsVerified(expense.getIsVerified());
        response.setIsManuallyEntered(expense.getIsManuallyEntered());
        response.setCreatedAt(expense.getCreatedAt());
        return response;
    }
}