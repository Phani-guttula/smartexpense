import { useAuth } from '../context/AuthContext';
import Button from '../components/Common/Button';

const Dashboard = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">
            💰 SmartExpense
          </h1>
          <div className="flex items-center space-x-4">
            <span className="text-gray-700">
              Welcome, <span className="font-semibold">{user?.fullName}</span>
            </span>
            <Button onClick={logout} variant="outline">
              Logout
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Dashboard</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {/* Card 1 */}
            <div className="bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-lg p-6 text-white">
              <p className="text-sm opacity-90">Total Expenses</p>
              <p className="text-3xl font-bold mt-2">₹0</p>
              <p className="text-sm mt-2 opacity-75">This month</p>
            </div>

            {/* Card 2 */}
            <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg p-6 text-white">
              <p className="text-sm opacity-90">Tax Saved</p>
              <p className="text-3xl font-bold mt-2">₹0</p>
              <p className="text-sm mt-2 opacity-75">Estimated</p>
            </div>

            {/* Card 3 */}
            <div className="bg-gradient-to-br from-amber-500 to-amber-600 rounded-lg p-6 text-white">
              <p className="text-sm opacity-90">Receipts</p>
              <p className="text-3xl font-bold mt-2">0</p>
              <p className="text-sm mt-2 opacity-75">Uploaded</p>
            </div>
          </div>

          <div className="text-center py-12 bg-gray-50 rounded-lg">
            <p className="text-gray-600 mb-4">
              🎉 Authentication is working! Dashboard coming soon...
            </p>
            <p className="text-sm text-gray-500">
              User ID: {user?.userId} | Email: {user?.email}
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;