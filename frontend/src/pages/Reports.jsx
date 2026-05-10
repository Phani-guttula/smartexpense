import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import ITRReportDownload from '../components/Reports/ITRReportDownload';
import Button from '../components/Common/Button';

/**
 * Reports page - Central hub for all reports
 */
const Reports = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Reports</h1>
              <p className="text-sm text-gray-600 mt-1">
                Generate and download tax reports
              </p>
            </div>
            
            <div className="flex items-center gap-3">
              <Button
                variant="outline"
                onClick={() => navigate('/dashboard')}
              >
                ← Back to Dashboard
              </Button>
              
              <Button onClick={logout} variant="outline">
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-6">
          {/* ITR Report */}
          <ITRReportDownload />

          {/* Future Reports - Placeholder */}
          <div className="bg-white rounded-xl shadow-md p-6 opacity-60">
            <div className="flex items-start gap-4">
              <div className="text-4xl">📈</div>
              <div className="flex-1">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                  Monthly Expense Report
                </h2>
                <p className="text-gray-600 mb-4">
                  Detailed month-by-month breakdown of all expenses
                </p>
                <Button disabled variant="outline">
                  Coming Soon
                </Button>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 opacity-60">
            <div className="flex items-start gap-4">
              <div className="text-4xl">💰</div>
              <div className="flex-1">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                  Budget Analysis Report
                </h2>
                <p className="text-gray-600 mb-4">
                  Compare actual spending vs planned budget
                </p>
                <Button disabled variant="outline">
                  Coming Soon
                </Button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Reports;