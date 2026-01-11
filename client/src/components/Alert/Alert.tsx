import React from 'react';

interface AlertProps {
  message: string;
  type: 'success' | 'error';
  onClose: () => void;
}

export const Alert: React.FC<AlertProps> = ({ message, type, onClose }) => {
  const bgColor = type === 'success' ? 'bg-green-50 border-green-200 text-green-800' : 'bg-red-50 border-red-200 text-red-700';
  const textColor = type === 'success' ? 'text-green-600 hover:text-green-800' : 'text-red-600 hover:text-red-800';

  return (
    <div className="max-w-7xl mx-auto px-4 pt-4">
      <div className={`${bgColor} border px-4 py-3 rounded-lg flex justify-between items-center`}>
        <span>{message}</span>
        <button onClick={onClose} className={textColor}>
          ✕
        </button>
      </div>
    </div>
  );
};  