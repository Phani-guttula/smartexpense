import api from './api';

/**
 * Expense service - handles all expense-related API calls
 * Created: Day 2
 * TODO: Add receipt upload functionality later
 */
const expenseService = {
  
  // Fetch all expenses with optional filters
  // Returns paginated results from backend
  getExpenses: async (filters = {}) => {
    try {
      const { categoryCode, startDate, endDate, page = 0, size = 20 } = filters;
      
      // Build query params - only include if they exist
      const params = new URLSearchParams();
      params.append('page', page);
      params.append('size', size);
      
      if (categoryCode) params.append('categoryCode', categoryCode);
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);
      
      const response = await api.get(`/expenses?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching expenses:', error);
      throw error;
    }
  },

  // Create new expense
  createExpense: async (expenseData) => {
    try {
      const response = await api.post('/expenses', expenseData);
      return response.data;
    } catch (error) {
      console.error('Error creating expense:', error);
      throw error;
    }
  },

  // Update existing expense
  updateExpense: async (id, expenseData) => {
    try {
      const response = await api.put(`/expenses/${id}`, expenseData);
      return response.data;
    } catch (error) {
      console.error('Error updating expense:', error);
      throw error;
    }
  },

  // Delete expense
  deleteExpense: async (id) => {
    try {
      const response = await api.delete(`/expenses/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting expense:', error);
      throw error;
    }
  },

  // Get single expense by ID
  getExpenseById: async (id) => {
    try {
      const response = await api.get(`/expenses/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching expense:', error);
      throw error;
    }
  },

  // Fetch dashboard data
  getDashboard: async () => {
    try {
      const response = await api.get('/dashboard');
      return response.data;
    } catch (error) {
      console.error('Error fetching dashboard:', error);
      throw error;
    }
  },

  // Fetch all categories
  getCategories: async () => {
    try {
      const response = await api.get('/categories');
      return response.data;
    } catch (error) {
      console.error('Error fetching categories:', error);
      throw error;
    }
  }
};

export default expenseService;