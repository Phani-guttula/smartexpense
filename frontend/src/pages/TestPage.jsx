import { useState, useEffect } from 'react';
import api from '../services/api';

function TestPage() {
  const [status, setStatus] = useState('Loading...');

  useEffect(() => {
    api.get('/test/health')
      .then(response => {
        setStatus(response.data.message);
      })
      .catch(error => {
        setStatus('Error: ' + error.message);
      });
  }, []);

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold">Backend Connection Test</h1>
      <p className="mt-4">Status: {status}</p>
    </div>
  );
}

export default TestPage;