'use client';

import { FeatureFlag } from '@/lib/api';
import FeatureFlagCard from './FeatureFlagCard';

interface FeatureFlagListProps {
  flags: FeatureFlag[];
  onUpdate: () => void;
  onDelete: () => void;
}

export default function FeatureFlagList({ flags, onUpdate, onDelete }: FeatureFlagListProps) {
  if (flags.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-12 text-center">
        <div className="text-gray-400 mb-4">
          <svg
            className="mx-auto h-12 w-12"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No feature flags</h3>
        <p className="text-gray-500">Get started by creating your first feature flag</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {flags.map((flag) => (
        <FeatureFlagCard
          key={flag.id}
          flag={flag}
          onUpdate={onUpdate}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}

