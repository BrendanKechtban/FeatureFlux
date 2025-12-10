'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { featureFlagApi, FeatureFlag } from '@/lib/api';
import FeatureFlagList from '@/components/FeatureFlagList';
import CreateFlagModal from '@/components/CreateFlagModal';
import Header from '@/components/Header';
import StatsDashboard from '@/components/StatsDashboard';

export default function Home() {
  const router = useRouter();
  const [flags, setFlags] = useState<FeatureFlag[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [user, setUser] = useState<{ username: string; role: string } | null>(null);


  const loadFlags = async () => {
    try {
      setLoading(true);
      const data = await featureFlagApi.getAll();
      setFlags(data);
    } catch (error) {
      console.error('Failed to load flags:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (!token || !userData) {
      router.push('/login');
      return;
    }
    try {
      setUser(JSON.parse(userData));
      // Only load flags if authenticated
      loadFlags();
      // Poll for updates every 5 seconds
      const interval = setInterval(loadFlags, 5000);
      return () => clearInterval(interval);
    } catch (e) {
      router.push('/login');
    }
  }, [router, refreshKey]);

  const handleFlagCreated = () => {
    setShowCreateModal(false);
    setRefreshKey((k) => k + 1);
  };

  const handleFlagUpdated = () => {
    setRefreshKey((k) => k + 1);
  };

  const handleFlagDeleted = () => {
    setRefreshKey((k) => k + 1);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Feature Flags</h1>
            <p className="text-gray-600 mt-2">
              Manage feature toggles and rollouts in real-time
            </p>
          </div>
          {user?.role === 'ADMIN' && (
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-primary-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary-700 transition-colors shadow-md"
            >
              + Create Flag
            </button>
          )}
        </div>

        <StatsDashboard flags={flags} />

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-gray-600">Loading feature flags...</p>
          </div>
        ) : (
          <FeatureFlagList
            flags={flags}
            onUpdate={handleFlagUpdated}
            onDelete={handleFlagDeleted}
          />
        )}

        {showCreateModal && (
          <CreateFlagModal
            onClose={() => setShowCreateModal(false)}
            onCreated={handleFlagCreated}
          />
        )}
      </main>
    </div>
  );
}

