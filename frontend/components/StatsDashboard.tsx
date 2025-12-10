'use client';

import { FeatureFlag } from '@/lib/api';

interface StatsDashboardProps {
  flags: FeatureFlag[];
}

export default function StatsDashboard({ flags }: StatsDashboardProps) {
  const totalFlags = flags.length;
  const enabledFlags = flags.filter((f) => f.enabled).length;
  const activeRollouts = flags.filter((f) => f.enabled && f.rolloutPercentage > 0 && f.rolloutPercentage < 100).length;
  const fullRollouts = flags.filter((f) => f.enabled && f.rolloutPercentage === 100).length;

  const stats = [
    {
      label: 'Total Flags',
      value: totalFlags,
      color: 'bg-blue-500',
    },
    {
      label: 'Enabled',
      value: enabledFlags,
      color: 'bg-green-500',
    },
    {
      label: 'Staged Rollouts',
      value: activeRollouts,
      color: 'bg-yellow-500',
    },
    {
      label: 'Full Rollouts',
      value: fullRollouts,
      color: 'bg-purple-500',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      {stats.map((stat) => (
        <div
          key={stat.label}
          className="bg-white rounded-lg shadow-md p-6 border-l-4"
          style={{ borderLeftColor: stat.color.replace('bg-', '').split('-')[1] === 'blue' ? '#3b82f6' : stat.color.replace('bg-', '').split('-')[1] === 'green' ? '#10b981' : stat.color.replace('bg-', '').split('-')[1] === 'yellow' ? '#eab308' : '#a855f7' }}
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">{stat.label}</p>
              <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
            </div>
            <div className={`w-12 h-12 ${stat.color} rounded-full opacity-20`}></div>
          </div>
        </div>
      ))}
    </div>
  );
}

