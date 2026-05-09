import { formatCurrency, formatDate } from '../../utils/formatters';

/**
 * Single expense item in the list
 * Shows expense details with edit/delete actions
 */
const ExpenseListItem = ({ expense, onEdit, onDelete }) => {
  
  // Category color coding for visual identification
  const getCategoryColor = (categoryCode) => {
    const colors = {
      '80C': 'bg-blue-100 text-blue-800',
      '80D': 'bg-green-100 text-green-800',
      'MEDICAL': 'bg-green-100 text-green-800',
      'FUEL': 'bg-amber-100 text-amber-800',
      'BUSINESS': 'bg-purple-100 text-purple-800',
      'OTHERS': 'bg-gray-100 text-gray-800'
    };
    
    return colors[categoryCode] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
      <div className="flex items-start justify-between">
        {/* Left side - Expense details */}
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-2">
            {/* Category badge */}
            <span className={`px-2 py-1 text-xs font-medium rounded ${getCategoryColor(expense.categoryCode)}`}>
              {expense.categoryName || expense.categoryCode}
            </span>
            
            {/* Date */}
            <span className="text-sm text-gray-500">
              {formatDate(expense.expenseDate)}
            </span>
          </div>

          {/* Merchant name */}
          {expense.merchantName && (
            <p className="font-medium text-gray-900 mb-1">
              {expense.merchantName}
            </p>
          )}

          {/* Description */}
          {expense.description && (
            <p className="text-sm text-gray-600 mb-2">
              {expense.description}
            </p>
          )}

          {/* Sub category if exists */}
          {expense.subCategory && (
            <span className="text-xs text-gray-500">
              {expense.subCategory}
            </span>
          )}
        </div>

        {/* Right side - Amount and actions */}
        <div className="text-right ml-4">
          {/* Amount */}
          <p className="text-lg font-bold text-gray-900 mb-2">
            {formatCurrency(expense.amount)}
          </p>

          {/* Action buttons */}
          <div className="flex gap-2 justify-end">
            <button
              onClick={() => onEdit(expense)}
              className="p-2 text-gray-600 hover:text-primary hover:bg-indigo-50 rounded transition"
              title="Edit expense"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>

            <button
              onClick={() => onDelete(expense)}
              className="p-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded transition"
              title="Delete expense"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>

          {/* Verification badge */}
          {expense.isVerified && (
            <div className="mt-2">
              <span className="text-xs text-green-600 flex items-center justify-end gap-1">
                <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                Verified
              </span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ExpenseListItem;