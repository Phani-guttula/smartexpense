/**
 * Empty state component - shown when no data exists
 */
const EmptyState = ({ 
  icon = '📭', 
  title = 'No data found', 
  message = 'Get started by adding your first item',
  actionLabel,
  onAction 
}) => {
  return (
    <div className="text-center py-12">
      <div className="text-6xl mb-4">{icon}</div>
      <h3 className="text-xl font-semibold text-gray-800 mb-2">{title}</h3>
      <p className="text-gray-600 mb-6">{message}</p>
      {actionLabel && onAction && (
        <button
          onClick={onAction}
          className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-indigo-700 transition"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export default EmptyState;