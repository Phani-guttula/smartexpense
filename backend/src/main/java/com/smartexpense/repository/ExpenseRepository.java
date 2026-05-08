package com.smartexpense.repository;

import com.smartexpense.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // Find all expenses for a user
    Page<Expense> findByUserId(Long userId, Pageable pageable);
    
    // Find expenses by user and category
    Page<Expense> findByUserIdAndCategoryCode(Long userId, String categoryCode, Pageable pageable);
    
    // Find expenses by user and date range
    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // Get total expenses by user
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    BigDecimal getTotalExpensesByUserId(@Param("userId") Long userId);
    
    // Get total expenses by user and category
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId AND e.categoryCode = :categoryCode")
    BigDecimal getTotalExpensesByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("categoryCode") String categoryCode
    );
    
    // Get expense count by user
    Long countByUserId(Long userId);
    
    // Dashboard query - Category-wise breakdown
    @Query("SELECT e.categoryCode, SUM(e.amount) " +
           "FROM Expense e WHERE e.userId = :userId " +
           "GROUP BY e.categoryCode")
    List<Object[]> getCategoryWiseBreakdown(@Param("userId") Long userId);
    
    // Monthly expenses
    @Query("SELECT FUNCTION('MONTH', e.expenseDate) as month, SUM(e.amount) " +
           "FROM Expense e WHERE e.userId = :userId " +
           "AND FUNCTION('YEAR', e.expenseDate) = :year " +
           "GROUP BY FUNCTION('MONTH', e.expenseDate) " +
           "ORDER BY month")
    List<Object[]> getMonthlyExpenses(@Param("userId") Long userId, @Param("year") int year);
}