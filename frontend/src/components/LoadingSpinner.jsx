import React from 'react';

export default function LoadingSpinner({ message = 'Loading...' }) {
  return (
    <div className="loading-box" role="status">
      <p>{message}</p>
    </div>
  );
}
