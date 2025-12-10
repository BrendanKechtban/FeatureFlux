'use client';

import { useState, useEffect } from 'react';
import { FeatureFlag, featureFlagApi } from '@/lib/api';
import { format } from 'date-fns';
import EditFlagModal from './EditFlagModal';

interface FeatureFlagCardProps {
  flag: FeatureFlag;
  onUpdate: () => void;
  onDelete: () => void;
}

const isAdmin = () => {
  if (typeof window === 'undefined') return false;
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user).role === 'ADMIN' : false;
};

export default function FeatureFlagCard({ flag, onUpdate, onDelete }: FeatureFlagCardProps) {
  const [showEditModal, setShowEditModal] = useState(false);
  const [toggling, setToggling] = useState(false);
  const [admin, setAdmin] = useState(false);

  useEffect(() => {
    setAdmin(isAdmin());
  }, []);

  const handleToggle = async () => {
    try {
      setToggling(true);
      await featureFlagApi.toggle(flag.key, !flag.enabled);
      onUpdate();
    } catch (error) {
      console.error('Failed to toggle flag:', error);
      alert('Failed to toggle flag');
    } finally {
      setToggling(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm(`Are you sure you want to delete "${flag.name}"?`)) {
      return;
    }

    try {
      await featureFlagApi.delete(flag.key);
      onDelete();
    } catch (error) {
      console.error('Failed to delete flag:', error);
      alert('Failed to delete flag');
    }
  };

  return (
    <>
      <div className="bg-white rounded-lg shadow-md p-6 border-l-4 border-l-primary-500 hover:shadow-lg transition-shadow">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center space-x-3 mb-2">
              <h3 className="text-xl font-semibold text-gray-900">{flag.name}</h3>
              <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-700">
                {flag.key}
              </span>
              <span
                className={`px-2 py-1 text-xs font-medium rounded-full ${
                  flag.enabled
                    ? 'bg-green-100 text-green-800'
                    : 'bg-gray-100 text-gray-800'
                }`}
              >
                {flag.enabled ? 'Enabled' : 'Disabled'}
              </span>
            </div>

            {flag.description && (
              <p className="text-gray-600 mb-4">{flag.description}</p>
            )}

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
              <div>
                <p className="text-xs text-gray-500 mb-1">Rollout</p>
                <div className="flex items-center space-x-2">
                  <div className="flex-1 bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-primary-600 h-2 rounded-full transition-all"
                      style={{ width: `${flag.rolloutPercentage}%` }}
                    ></div>
                  </div>
                  <span className="text-sm font-medium text-gray-700">
                    {flag.rolloutPercentage}%
                  </span>
                </div>
              </div>

              <div>
                <p className="text-xs text-gray-500 mb-1">Target Users</p>
                <p className="text-sm font-medium text-gray-700">
                  {flag.targetUserIds?.length || 0}
                </p>
              </div>

              <div>
                <p className="text-xs text-gray-500 mb-1">Excluded Users</p>
                <p className="text-sm font-medium text-gray-700">
                  {flag.excludedUserIds?.length || 0}
                </p>
              </div>

              <div>
                <p className="text-xs text-gray-500 mb-1">Last Updated</p>
                <p className="text-sm font-medium text-gray-700">
                  {format(new Date(flag.updatedAt), 'MMM d, HH:mm')}
                </p>
              </div>
            </div>
          </div>

          {admin && (
            <div className="flex items-center space-x-2 ml-4">
              <button
                onClick={handleToggle}
                disabled={toggling}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  flag.enabled
                    ? 'bg-red-100 text-red-700 hover:bg-red-200'
                    : 'bg-green-100 text-green-700 hover:bg-green-200'
                } disabled:opacity-50`}
              >
                {toggling ? '...' : flag.enabled ? 'Disable' : 'Enable'}
              </button>
              <button
                onClick={() => setShowEditModal(true)}
                className="px-4 py-2 rounded-lg font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
              >
                Edit
              </button>
              <button
                onClick={handleDelete}
                className="px-4 py-2 rounded-lg font-medium bg-red-100 text-red-700 hover:bg-red-200 transition-colors"
              >
                Delete
              </button>
            </div>
          )}
        </div>
      </div>

      {showEditModal && (
        <EditFlagModal
          flag={flag}
          onClose={() => setShowEditModal(false)}
          onUpdated={onUpdate}
        />
      )}
    </>
  );
}

