import { useState, useRef } from 'react';
import { toast } from 'react-hot-toast';
import api from '../../services/api';
import Button from '../Common/Button';
import LoadingSpinner from '../Common/LoadingSpinner';

/**
 * Receipt upload component with drag-and-drop
 * Handles file upload and OCR processing
 */
const ReceiptUpload = ({ onOCRComplete }) => {
  
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [preview, setPreview] = useState(null);
  const fileInputRef = useRef(null);

  // Handle file selection
  const handleFileSelect = (file) => {
    if (!file) return;

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf'];
    if (!validTypes.includes(file.type)) {
      toast.error('Please upload a JPG, PNG, or PDF file');
      return;
    }

    // Validate file size (5MB max)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      toast.error('File size must be less than 5MB');
      return;
    }

    // Show preview for images
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreview(e.target.result);
      };
      reader.readAsDataURL(file);
    }

    // Upload and process
    uploadReceipt(file);
  };

  // Upload receipt to backend
  const uploadReceipt = async (file) => {
    setUploading(true);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/receipts/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      if (response.data.success) {
        const { receiptUrl, ocrResult, requiresManualReview } = response.data.data;

        if (requiresManualReview) {
          toast('Receipt scanned! Please verify the extracted data.', {
            icon: '⚠️',
            duration: 4000
          });
        } else {
          toast.success('Receipt scanned successfully!');
        }

        // Pass OCR results to parent component
        if (onOCRComplete) {
          onOCRComplete({
            receiptUrl,
            ...ocrResult
          });
        }

        // Clear preview after 2 seconds
        setTimeout(() => {
          setPreview(null);
        }, 2000);
      }

    } catch (error) {
      console.error('Upload failed:', error);
      toast.error(error.response?.data?.message || 'Failed to process receipt');
    } finally {
      setUploading(false);
    }
  };

  // Drag and drop handlers
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      handleFileSelect(e.target.files[0]);
    }
  };

  const handleButtonClick = () => {
    fileInputRef.current.click();
  };

  return (
    <div className="w-full">
      {/* Upload Area */}
      <div
        className={`relative border-2 border-dashed rounded-lg p-8 text-center transition ${
          dragActive 
            ? 'border-primary bg-indigo-50' 
            : 'border-gray-300 hover:border-gray-400'
        }`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
      >
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept="image/jpeg,image/jpg,image/png,application/pdf"
          onChange={handleChange}
          disabled={uploading}
        />

        {uploading ? (
          <LoadingSpinner message="Processing receipt..." />
        ) : preview ? (
          <div className="space-y-4">
            <img 
              src={preview} 
              alt="Receipt preview" 
              className="max-h-64 mx-auto rounded-lg shadow-md"
            />
            <p className="text-sm text-gray-600">Processing...</p>
          </div>
        ) : (
          <>
            {/* Upload Icon */}
            <div className="mb-4">
              <svg 
                className="mx-auto h-12 w-12 text-gray-400" 
                stroke="currentColor" 
                fill="none" 
                viewBox="0 0 48 48"
              >
                <path 
                  d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" 
                  strokeWidth={2} 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                />
              </svg>
            </div>

            <div className="mb-4">
              <p className="text-lg font-medium text-gray-900 mb-2">
                📸 Upload Receipt Image
              </p>
              <p className="text-sm text-gray-600">
                Drag and drop your receipt here, or click to browse
              </p>
            </div>

            <Button onClick={handleButtonClick} variant="primary">
              Choose File
            </Button>

            <p className="mt-4 text-xs text-gray-500">
              Supported formats: JPG, PNG, PDF (Max 5MB)
            </p>
          </>
        )}
      </div>

      {/* How it works */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-sm font-semibold text-blue-900 mb-2">
          🤖 How OCR Works
        </h4>
        <ul className="text-xs text-blue-800 space-y-1">
          <li>✓ Upload a clear photo of your receipt</li>
          <li>✓ Our AI extracts amount, date, and merchant automatically</li>
          <li>✓ Review and confirm the extracted data</li>
          <li>✓ Save time - no manual typing needed!</li>
        </ul>
      </div>
    </div>
  );
};

export default ReceiptUpload;