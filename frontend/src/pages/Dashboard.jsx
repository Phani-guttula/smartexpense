import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import expenseService from '../services/expenseService';
import { formatCurrency } from '../utils/formatters';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import Button from '../components/Common/Button';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { LineChart, Line, XAxis, YAxis, CartesianGrid } from 'recharts';

/**
 * Main dashboard page
 * Shows expense summary, charts, and quick actions
 */
const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await expenseService.getDashboard();
      
      if (response.success) {
        setDashboardData(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  // Colors for pie chart - keeping it simple
  const COLORS = ['#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'];

  // Prepare data for charts
  const pieChartData = dashboardData?.categoryBreakdown?.map((item, index) => ({
    name: item.categoryName,
    value: parseFloat(item.amount),
    percentage: item.percentage
  })) || [];

  const lineChartData = dashboardData?.monthlyTrend || [];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                💰 SmartExpense
              </h1>
              <p className="text-sm text-gray-600 mt-1">
                Welcome back, {user?.fullName}
              </p>
            </div>
            
            <div className="flex items-center gap-3">
              <Button
                variant="outline"
                onClick={() => navigate('/expenses')}
              >
                View All Expenses
              </Button>
              
              <Button onClick={logout} variant="outline">
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loading ? (
          <LoadingSpinner message="Loading dashboard..." />
        ) : (
          <div className="space-y-6">
            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Total Expenses */}
              <div className="bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-xl p-6 text-white shadow-lg">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm opacity-90">Total Expenses</p>
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                          d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <p className="text-3xl font-bold">
                  {formatCurrency(dashboardData?.totalExpenses || 0)}
                </p>
                <p className="text-sm mt-2 opacity-75">This financial year</p>
              </div>

              {/* Tax Saved */}
              <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white shadow-lg">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm opacity-90">Tax Saved</p>
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <p className="text-3xl font-bold">
                  {formatCurrency(dashboardData?.estimatedTaxSaving || 0)}
                </p>
                <p className="text-sm mt-2 opacity-75">Estimated (20% bracket)</p>
              </div>

              {/* Receipts */}
              <div className="bg-gradient-to-br from-amber-500 to-amber-600 rounded-xl p-6 text-white shadow-lg">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm opacity-90">Receipts</p>
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <p className="text-3xl font-bold">
                  {dashboardData?.receiptCount || 0}
                </p>
                <p className="text-sm mt-2 opacity-75">Uploaded</p>
              </div>
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Category Breakdown - Pie Chart */}
              <div className="bg-white rounded-xl shadow-md p-6">
                <h3 className="text-lg font-semibold mb-4">Category Breakdown</h3>
                
                {pieChartData.length === 0 ? (
                  <div className="text-center py-12 text-gray-500">
                    No expenses yet. Add your first expense to see the breakdown.
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={pieChartData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percentage }) => `${name} (${percentage?.toFixed(0)}%)`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {pieChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => formatCurrency(value)} />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </div>

              {/* Monthly Trend - Line Chart */}
              <div className="bg-white rounded-xl shadow-md p-6">
                <h3 className="text-lg font-semibold mb-4">Monthly Trend</h3>
                
                {lineChartData.length === 0 ? (
                  <div className="text-center py-12 text-gray-500">
                    Add expenses to see monthly trends
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={lineChartData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip formatter={(value) => formatCurrency(value)} />
                      <Line 
                        type="monotone" 
                        dataKey="amount" 
                        stroke="#4F46E5" 
                        strokeWidth={2}
                        dot={{ fill: '#4F46E5', r: 4 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <h3 className="text-lg font-semibold mb-4">Quick Actions</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <button
                  onClick={() => navigate('/expenses')}
                  className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary hover:bg-indigo-50 transition text-left"
                >
                  <div className="text-2xl mb-2">📝</div>
                  <h4 className="font-semibold text-gray-900">Add Expense</h4>
                  <p className="text-sm text-gray-600 mt-1">
                    Track a new expense
                  </p>
                </button>

                {/* UPDATED - Change from toast to navigate */}
                <button
                  onClick={() => navigate('/reports')}
                  className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary hover:bg-indigo-50 transition text-left"
                >
                  <div className="text-2xl mb-2">📊</div>
                  <h4 className="font-semibold text-gray-900">ITR Report</h4>
                  <p className="text-sm text-gray-600 mt-1">
                    Download tax report
                  </p>
                </button>

                <button
                  onClick={() => toast.info('Receipt upload feature coming soon!')}
                  className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary hover:bg-indigo-50 transition text-left"
                >
                  <div className="text-2xl mb-2">📸</div>
                  <h4 className="font-semibold text-gray-900">Upload Receipt</h4>
                  <p className="text-sm text-gray-600 mt-1">
                    Scan and extract data
                  </p>
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;