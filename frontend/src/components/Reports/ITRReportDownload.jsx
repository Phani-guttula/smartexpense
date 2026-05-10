import { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import api from '../../services/api';
import Button from '../Common/Button';
import LoadingSpinner from '../Common/LoadingSpinner';

/**
 * ITR Report download component
 * Allows users to generate and download Excel reports
 */
const ITRReportDownload = () => {
  
  const [financialYears, setFinancialYears] = useState([]);
  const [selectedYear, setSelectedYear] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetchingYears, setFetchingYears] = useState(true);

  useEffect(() => {
    fetchFinancialYears();
  }, []);

  const fetchFinancialYears = async () => {
    try {
      const response = await api.get('/reports/financial-years');
      setFinancialYears(response.data);
      
      // Set current financial year as default
      if (response.data && response.data.length > 0) {
        setSelectedYear(response.data[0]);
      }
    } catch (error) {
      console.error('Failed to fetch financial years:', error);
      toast.error('Failed to load financial years');
    } finally {
      setFetchingYears(false);
    }
  };

  const handleDownload = async () => {
    if (!selectedYear) {
      toast.error('Please select a financial year');
      return;
    }

    setLoading(true);
    toast.loading('Generating report...', { id: 'report-download' });

    try {
      // Make request to download Excel file
      const response = await api.get(`/reports/itr?financialYear=${selectedYear}`, {
        responseType: 'blob' // Important for file download
      });

      // Create blob from response
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `ITR_Report_FY${selectedYear}_${new Date().toISOString().split('T')[0]}.xlsx`;
      
      // Trigger download
      document.body.appendChild(link);
      link.click();
      
      // Cleanup
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      toast.success('Report downloaded successfully!', { id: 'report-download' });

    } catch (error) {
      console.error('Download failed:', error);
      toast.error('Failed to generate report. Please try again.', { id: 'report-download' });
    } finally {
      setLoading(false);
    }
  };

  if (fetchingYears) {
    return <LoadingSpinner message="Loading..." />;
  }

  return (
    <div className="bg-white rounded-xl shadow-md p-6">
      <div className="flex items-start gap-4 mb-6">
        <div className="text-4xl">📊</div>
        <div className="flex-1">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Download ITR Report
          </h2>
          <p className="text-gray-600">
            Generate a comprehensive Excel report with all your deductible expenses for tax filing
          </p>
        </div>
      </div>

      {/* Financial Year Selector */}
      <div className="mb-6">
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Select Financial Year
        </label>
        <select
          value={selectedYear}
          onChange={(e) => setSelectedYear(e.target.value)}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
          disabled={loading}
        >
          {financialYears.map(year => (
            <option key={year} value={year}>
              FY 20{year} (Apr 20{year.split('-')[0]} - Mar 20{year.split('-')[1]})
            </option>
          ))}
        </select>
      </div>

      {/* Download Button */}
      <Button
        onClick={handleDownload}
        loading={loading}
        fullWidth
      >
        {loading ? 'Generating Report...' : '📥 Download Excel Report'}
      </Button>

      {/* Report Contents Info */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-sm font-semibold text-blue-900 mb-3">
          📄 What's Included in the Report
        </h4>
        <ul className="text-sm text-blue-800 space-y-2">
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">✓</span>
            <span><strong>Summary Sheet:</strong> Total expenses, deductions, and tax savings</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">✓</span>
            <span><strong>Category Breakdown:</strong> Expenses grouped by ITR sections (80C, 80D, etc.)</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">✓</span>
            <span><strong>All Transactions:</strong> Complete list of expenses with dates and amounts</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">✓</span>
            <span><strong>Filing Instructions:</strong> Step-by-step guide for ITR filing</span>
          </li>
        </ul>
      </div>

      {/* Sample Preview */}
      <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <h4 className="text-sm font-semibold text-gray-700 mb-2">
          💡 Pro Tips
        </h4>
        <ul className="text-sm text-gray-600 space-y-1">
          <li>• Keep all original receipts for verification</li>
          <li>• Review category limits before filing</li>
          <li>• Consult a tax professional for complex cases</li>
          <li>• File your ITR before July 31st deadline</li>
        </ul>
      </div>
    </div>
  );
};

export default ITRReportDownload;