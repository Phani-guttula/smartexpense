const Button = ({ 
  children, 
  type = 'button', 
  onClick, 
  loading = false, 
  variant = 'primary',
  fullWidth = false,
  disabled = false,
  ...props 
}) => {
  const baseStyles = 'px-6 py-3 rounded-lg font-semibold transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed';
  
  const variants = {
    primary: 'bg-primary text-white hover:bg-indigo-700 focus:ring-4 focus:ring-indigo-300',
    secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300 focus:ring-4 focus:ring-gray-200',
    outline: 'border-2 border-primary text-primary hover:bg-primary hover:text-white'
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`${baseStyles} ${variants[variant]} ${fullWidth ? 'w-full' : ''}`}
      {...props}
    >
      {loading ? (
        <div className="flex items-center justify-center">
          <svg className="animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          Loading...
        </div>
      ) : (
        children
      )}
    </button>
  );
};

export default Button;