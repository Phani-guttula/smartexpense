import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import expenseService from '../../services/expenseService';
import ReceiptUpload from './ReceiptUpload';
import Input from '../Common/Input';
import Button from '../Common/Button';
import { formatDateForInput } from '../../utils/formatters';

/**
 * Enhanced expense form with OCR support
 * Allows both manual entry and receipt scanning
 */
const AddExpenseFormWithOCR = ({ onSuccess, onCancel, editExpense = null }) => {
  const isEditMode = !!editExpense;

  // Toggle between manual entry and OCR
  const [inputMode, setInputMode] = useState('manual'); // 'manual' or 'ocr'

  const [formData, setFormData] = useState({
    amount: '',
    categoryCode: '',
    subCategory: '',
    description: '',
    expenseDate: formatDateForInput(new Date()),
    merchantName: '',
    receiptUrl: ''
  });

  const [categories, setCategories] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [ocrConfidence, setOcrConfidence] = useState(null);

  // Load categories
  useEffect(() => {
    fetchCategories();
  }, []);

  // Populate form if editing
  useEffect(() => {
    if (editExpense) {
      setFormData({
        amount: editExpense.amount || '',
        categoryCode: editExpense.categoryCode || '',
        subCategory: editExpense.subCategory || '',
        description: editExpense.description || '',
        expenseDate: formatDateForInput(editExpense.expenseDate) || '',
        merchantName: editExpense.merchantName || '',
        receiptUrl: editExpense.receiptUrl || ''
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

  // Handle OCR completion
  const handleOCRComplete = (ocrData) => {
    console.log('OCR Data received:', ocrData);

    // Auto-fill form with OCR data
    setFormData(prev => ({
      ...prev,
      amount: ocrData.amount || prev.amount,
      expenseDate: ocrData.date ? formatDateForInput(ocrData.date) : prev.expenseDate,
      merchantName: ocrData.merchantName || prev.merchantName,
      categoryCode: ocrData.suggestedCategory || prev.categoryCode,
      receiptUrl: ocrData.receiptUrl || prev.receiptUrl
    }));

    // Store confidence for display
    setOcrConfidence(ocrData.overallConfidence);

    // Show notes if any
    if (ocrData.notes) {
      toast(ocrData.notes, { icon: 'ℹ️', duration: 5000 });
    }

    // Switch to manual mode to review
    setInputMode('manual');
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

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
        isManuallyEntered: inputMode === 'manual'
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
    <div className="space-y-6">
      {/* Mode Selector */}
      {!isEditMode && (
        <div className="flex gap-2 p-1 bg-gray-100 rounded-lg">
          <button
            type="button"
            onClick={() => setInputMode('manual')}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              inputMode === 'manual'
                ? 'bg-white text-primary shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            ✏️ Manual Entry
          </button>
          <button
            type="button"
            onClick={() => setInputMode('ocr')}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              inputMode === 'ocr'
                ? 'bg-white text-primary shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            📸 Scan Receipt
          </button>
        </div>
      )}

      {/* OCR Upload or Manual Form */}
      {inputMode === 'ocr' && !isEditMode ? (
        <ReceiptUpload onOCRComplete={handleOCRComplete} />
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* OCR Confidence Badge */}
          {ocrConfidence !== null && (
            <div className={`p-3 rounded-lg flex items-center gap-2 ${
              ocrConfidence >= 0.7 
                ? 'bg-green-50 border border-green-200' 
                : 'bg-yellow-50 border border-yellow-200'
            }`}>
              <svg className="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span className="text-sm">
                <strong>OCR Confidence:</strong> {Math.round(ocrConfidence * 100)}%
                {ocrConfidence < 0.7 && ' - Please verify the data below'}
              </span>
            </div>
          )}

          {/* Receipt Preview */}
          {formData.receiptUrl && (
            <div className="border rounded-lg p-3 bg-gray-50">
              <p className="text-sm font-medium text-gray-700 mb-2">Receipt Image:</p>
              <img 
                src={formData.receiptUrl} 
                alt="Receipt" 
                className="max-h-40 rounded border"
              />
            </div>
          )}

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
            <Button type="submit" loading={loading} fullWidth>
              {isEditMode ? 'Update Expense' : 'Save Expense'}
            </Button>
            
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel} fullWidth>
                Cancel
              </Button>
            )}
          </div>
        </form>
      )}
    </div>
  );
};

export default AddExpenseFormWithOCR;