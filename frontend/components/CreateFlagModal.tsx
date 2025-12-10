'use client';

import { useState } from 'react';
import { featureFlagApi } from '@/lib/api';

interface CreateFlagModalProps {
  onClose: () => void;
  onCreated: () => void;
}

export default function CreateFlagModal({ onClose, onCreated }: CreateFlagModalProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    key: '',
    name: '',
    description: '',
    enabled: false,
    rolloutPercentage: 0,
    targetUserIds: '',
    excludedUserIds: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await featureFlagApi.create({
        ...formData,
        targetUserIds: formData.targetUserIds
          ? formData.targetUserIds.split(',').map((id) => id.trim())
          : [],
        excludedUserIds: formData.excludedUserIds
          ? formData.excludedUserIds.split(',').map((id) => id.trim())
          : [],
      });
      onCreated();
    } catch (error) {
      console.error('Failed to create flag:', error);
      alert('Failed to create feature flag');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-900">Create Feature Flag</h2>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Key <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              value={formData.key}
              onChange={(e) => setFormData({ ...formData, key: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="my-feature-flag"
            />
            <p className="text-xs text-gray-500 mt-1">Unique identifier (lowercase, hyphens)</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="My Feature Flag"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              rows={3}
              placeholder="Describe what this feature flag controls..."
            />
          </div>

          <div className="flex items-center space-x-4">
            <div className="flex items-center">
              <input
                type="checkbox"
                id="enabled"
                checked={formData.enabled}
                onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <label htmlFor="enabled" className="ml-2 text-sm font-medium text-gray-700">
                Enabled
              </label>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Rollout Percentage
            </label>
            <input
              type="range"
              min="0"
              max="100"
              value={formData.rolloutPercentage}
              onChange={(e) =>
                setFormData({ ...formData, rolloutPercentage: parseInt(e.target.value) })
              }
              className="w-full"
            />
            <div className="flex justify-between text-xs text-gray-500 mt-1">
              <span>0%</span>
              <span className="font-medium">{formData.rolloutPercentage}%</span>
              <span>100%</span>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Target User IDs (comma-separated)
            </label>
            <input
              type="text"
              value={formData.targetUserIds}
              onChange={(e) => setFormData({ ...formData, targetUserIds: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="user1, user2, user3"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Excluded User IDs (comma-separated)
            </label>
            <input
              type="text"
              value={formData.excludedUserIds}
              onChange={(e) => setFormData({ ...formData, excludedUserIds: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="user1, user2, user3"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-2 border border-gray-300 rounded-lg font-medium text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Create Flag'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

