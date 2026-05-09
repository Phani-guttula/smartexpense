import { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import expenseService from '../services/expenseService';
import ExpenseListItem from '../components/Expenses/ExpenseListItem';
import AddExpenseForm from '../components/Expenses/AddExpenseForm';
import Modal from '../components/Common/Modal';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import EmptyState from '../components/Common/EmptyState';
import Button from '../components/Common/Button';

/**
 * Main expenses list page
 * Shows all user expenses with filtering options
 */
const ExpenseList = () => {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingExpense, setEditingExpense] = useState(null);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({
    categoryCode: '',
    page: 0
  });

  // Fetch expenses on component mount and when filters change
  useEffect(() => {
    fetchExpenses();
  }, [filters]);

  // Fetch categories for filter dropdown
  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchExpenses = async () => {
    setLoading(true);
    try {
      const response = await expenseService.getExpenses(filters);
      
      if (response.success) {
        // Backend returns paginated data
        setExpenses(response.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch expenses:', error);
      toast.error('Failed to load expenses');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await expenseService.getCategories();
      if (response.success) {
        setCategories(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch categories:', error);
    }
  };

  const handleAddSuccess = (newExpense) => {
    // Add new expense to list
    setExpenses(prev => [newExpense, ...prev]);
    setShowAddModal(false);
    
    // Refresh to get updated data
    fetchExpenses();
  };

  const handleEditClick = (expense) => {
    setEditingExpense(expense);
    setShowAddModal(true);
  };

  const handleEditSuccess = (updatedExpense) => {
    // Update expense in list
    setExpenses(prev => 
      prev.map(exp => exp.id === updatedExpense.id ? updatedExpense : exp)
    );
    setShowAddModal(false);
    setEditingExpense(null);
    
    // Refresh to get updated data
    fetchExpenses();
  };

  const handleDelete = async (expense) => {
    // Confirm before deleting
    const confirmed = window.confirm(
      `Are you sure you want to delete this expense of ${expense.amount}?`
    );

    if (!confirmed) return;

    try {
      const response = await expenseService.deleteExpense(expense.id);
      
      if (response.success) {
        toast.success('Expense deleted successfully');
        
        // Remove from list
        setExpenses(prev => prev.filter(exp => exp.id !== expense.id));
      }
    } catch (error) {
      console.error('Failed to delete expense:', error);
      toast.error('Failed to delete expense');
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value,
      page: 0 // Reset to first page when filter changes
    }));
  };

  const handleModalClose = () => {
    setShowAddModal(false);
    setEditingExpense(null);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">All Expenses</h1>
              <p className="text-sm text-gray-600 mt-1">
                {expenses.length} expense{expenses.length !== 1 ? 's' : ''} found
              </p>
            </div>
            
            <Button onClick={() => setShowAddModal(true)}>
              + Add Expense
            </Button>
          </div>

          {/* Filters */}
          <div className="mt-4 flex gap-4">
            <select
              name="categoryCode"
              value={filters.categoryCode}
              onChange={handleFilterChange}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="">All Categories</option>
              {categories.map(cat => (
                <option key={cat.code} value={cat.code}>
                  {cat.name}
                </option>
              ))}
            </select>

            {/* TODO: Add date range filter later */}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loading ? (
          <LoadingSpinner message="Loading expenses..." />
        ) : expenses.length === 0 ? (
          <EmptyState
            icon="💸"
            title="No expenses yet"
            message="Start tracking your expenses by adding your first expense"
            actionLabel="Add Your First Expense"
            onAction={() => setShowAddModal(true)}
          />
        ) : (
          <div className="space-y-3">
            {expenses.map(expense => (
              <ExpenseListItem
                key={expense.id}
                expense={expense}
                onEdit={handleEditClick}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}
      </main>

      {/* Add/Edit Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={handleModalClose}
        title={editingExpense ? 'Edit Expense' : 'Add New Expense'}
        size="lg"
      >
        <AddExpenseForm
          editExpense={editingExpense}
          onSuccess={editingExpense ? handleEditSuccess : handleAddSuccess}
          onCancel={handleModalClose}
        />
      </Modal>
    </div>
  );
};

export default ExpenseList;