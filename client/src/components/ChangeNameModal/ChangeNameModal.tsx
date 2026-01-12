import React, { useState } from 'react';

interface ChangeNameModalProps {
  isOpen: boolean;
  currentName: string;
  onClose: () => void;
  onChangeName: (newName: string) => void;
  loading: boolean;
  error: string;
}

export const ChangeNameModal: React.FC<ChangeNameModalProps> = ({
  isOpen,
  currentName,
  onClose,
  onChangeName,
  loading,
  error,
}) => {
  const [newName, setNewName] = useState('');

  const handleSubmit = () => {
    if (newName.trim() && newName !== currentName) {
      onChangeName(newName.trim());
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-md w-full p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">Change Username</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
            ✕
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Current Username
            </label>
            <input
              type="text"
              value={currentName}
              disabled
              className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
            />
          </div>

          <div>
            <label htmlFor="new-name" className="block text-sm font-medium text-gray-700 mb-2">
              New Username
            </label>
            <input
              id="new-name"
              type="text"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder="Enter new username"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              minLength={3}
            />
            <p className="mt-1 text-xs text-gray-500">
              Username must be at least 3 characters
            </p>
          </div>

          <div className="flex gap-3 pt-2">
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={loading || !newName.trim() || newName === currentName || newName.length < 3}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:bg-gray-400"
            >
              {loading ? 'Changing...' : 'Change Name'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};