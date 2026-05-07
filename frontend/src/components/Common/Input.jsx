const Input = ({ 
  label, 
  type = 'text', 
  value, 
  onChange, 
  error, 
  placeholder,
  required = false,
  ...props 
}) => {
  return (
    <div className="mb-4">
      {label && (
        <label className="block text-gray-700 text-sm font-semibold mb-2">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <input
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary transition ${
          error ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-primary'
        }`}
        {...props}
      />
      {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
    </div>
  );
};

export default Input;