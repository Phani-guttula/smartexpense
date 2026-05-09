import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import expenseService from '../../services/expenseService';
import Input from '../Common/Input';
import Button from '../Common/Button';
import { formatDateForInput } from '../../utils/formatters';

/**
 * Form to add new expense
 * Also used for editing existing expenses
 */
const AddExpenseForm = ({ onSuccess, onCancel, editExpense = null }) => {
  const isEditMode = !!editExpense;

  // Form state
  const [formData, setFormData] = useState({
    amount: '',
    categoryCode: '',
    subCategory: '',
    description: '',
    expenseDate: formatDateForInput(new Date()),
    merchantName: ''
  });

  const [categories, setCategories] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [loadingCategories, setLoadingCategories] = useState(true);

  // Load categories on mount
  useEffect(() => {
    fetchCategories();
  }, []);

  // If editing, populate form with existing data
  useEffect(() => {
    if (editExpense) {
      setFormData({
        amount: editExpense.amount || '',
        categoryCode: editExpense.categoryCode || '',
        subCategory: editExpense.subCategory || '',
        description: editExpense.description || '',
        expenseDate: formatDateForInput(editExpense.expenseDate) || '',
        merchantName: editExpense.merchantName || ''
      });
    }
  }, [editExpense]);

  const fetchCategories = async () => {
    try {
      const response = await expenseService.getCategories();
      if (response.success) {
        setCategories(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch categories:', error);
      toast.error('Failed to load categories');
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Basic client-side validation
  const validateForm = () => {
    const newErrors = {};

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Please enter a valid amount';
    }

    if (!formData.categoryCode) {
      newErrors.categoryCode = 'Please select a category';
    }

    if (!formData.expenseDate) {
      newErrors.expenseDate = 'Please select a date';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const expenseData = {
        ...formData,
        amount: parseFloat(formData.amount),
        isManuallyEntered: true
      };

      let response;
      if (isEditMode) {
        response = await expenseService.updateExpense(editExpense.id, expenseData);
        toast.success('Expense updated successfully!');
      } else {
        response = await expenseService.createExpense(expenseData);
        toast.success('Expense added successfully!');
      }

      if (response.success && onSuccess) {
        onSuccess(response.data);
      }

    } catch (error) {
      console.error('Error saving expense:', error);
      
      // Handle validation errors from backend
      if (error.response?.data?.data) {
        setErrors(error.response.data.data);
        toast.error('Please fix the errors in the form');
      } else {
        toast.error(error.response?.data?.message || 'Failed to save expense');
      }
    } finally {
      setLoading(false);
    }
  };

  if (loadingCategories) {
    return <div className="text-center py-8">Loading categories...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Amount */}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Amount <span className="text-red-500">*</span>
        </label>
        <div className="relative">
          <span className="absolute left-3 top-3 text-gray-500">₹</span>
          <input
            type="number"
            name="amount"
            value={formData.amount}
            onChange={handleChange}
            placeholder="0.00"
            step="0.01"
            className={`w-full pl-8 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary ${
              errors.amount ? 'border-red-500' : 'border-gray-300'
            }`}
          />
        </div>
        {errors.amount && <p className="text-red-500 text-sm mt-1">{errors.amount}</p>}
      </div>

      {/* Category */}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Category <span className="text-red-500">*</span>
        </label>
        <select
          name="categoryCode"
          value={formData.categoryCode}
          onChange={handleChange}
          className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary ${
            errors.categoryCode ? 'border-red-500' : 'border-gray-300'
          }`}
        >
          <option value="">Select category</option>
          {categories.map(cat => (
            <option key={cat.code} value={cat.code}>
              {cat.name} {cat.itrSection ? `(${cat.itrSection})` : ''}
            </option>
          ))}
        </select>
        {errors.categoryCode && <p className="text-red-500 text-sm mt-1">{errors.categoryCode}</p>}
      </div>

      {/* Date */}
      <Input
        label="Date"
        type="date"
        name="expenseDate"
        value={formData.expenseDate}
        onChange={handleChange}
        error={errors.expenseDate}
        required
      />

      {/* Merchant Name */}
      <Input
        label="Merchant/Vendor"
        type="text"
        name="merchantName"
        value={formData.merchantName}
        onChange={handleChange}
        placeholder="Apollo Pharmacy, LIC, etc."
      />

      {/* Sub Category */}
      <Input
        label="Sub Category (Optional)"
        type="text"
        name="subCategory"
        value={formData.subCategory}
        onChange={handleChange}
        placeholder="e.g., Medicines, Premium Payment"
      />

      {/* Description */}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Description (Optional)
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows="3"
          placeholder="Add any notes about this expense..."
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary resize-none"
        />
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 pt-4">
        <Button
          type="submit"
          loading={loading}
          fullWidth
        >
          {isEditMode ? 'Update Expense' : 'Add Expense'}
        </Button>
        
        {onCancel && (
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            fullWidth
          >
            Cancel
          </Button>
        )}
      </div>
    </form>
  );
};

export default AddExpenseForm;