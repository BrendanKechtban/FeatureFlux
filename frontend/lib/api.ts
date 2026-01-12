import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface FeatureFlag {
  id: number;
  key: string;
  name: string;
  description: string;
  enabled: boolean;
  rolloutPercentage: number;
  targetUserIds: string[];
  excludedUserIds: string[];
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EvaluationResponse {
  flagKey: string;
  userId: string;
  enabled: boolean;
  bucket: number;
}

export const featureFlagApi = {
  getAll: async (): Promise<FeatureFlag[]> => {
    const response = await api.get('/flags');
    return response.data;
  },

  getById: async (id: number): Promise<FeatureFlag> => {
    const response = await api.get(`/flags/${id}`);
    return response.data;
  },

  getByKey: async (key: string): Promise<FeatureFlag> => {
    const response = await api.get(`/flags/key/${key}`);
    return response.data;
  },

  create: async (flag: Partial<FeatureFlag>): Promise<FeatureFlag> => {
    const response = await api.post('/flags', flag);
    return response.data;
  },

  update: async (id: number, flag: Partial<FeatureFlag>): Promise<FeatureFlag> => {
    const response = await api.put(`/flags/${id}`, flag);
    return response.data;
  },

  delete: async (key: string): Promise<void> => {
    await api.delete(`/flags/${key}`);
  },

  toggle: async (key: string, enabled: boolean): Promise<FeatureFlag> => {
    const response = await api.post(`/flags/${key}/toggle`, { enabled });
    return response.data;
  },

  evaluate: async (flagKey: string, userId: string): Promise<EvaluationResponse> => {
    const response = await api.post('/evaluate', { flagKey, userId });
    return response.data;
  },
};

export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export const authApi = {
  login: async (credentials: AuthRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (credentials: AuthRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', credentials);
    return response.data;
  },
};

export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityKey: string;
  performedBy: string;
  timestamp: string;
  description: string;
}

export interface KillSwitch {
  id: number;
  flagKey: string;
  active: boolean;
  reason: string;
  activatedBy: string;
}

export const adminApi = {
  getAuditLogs: async (flagKey?: string): Promise<AuditLog[]> => {
    const url = flagKey ? `/audit/flag/${flagKey}` : '/audit/recent';
    const response = await api.get(url);
    return response.data;
  },

  activateKillSwitch: async (flagKey: string, reason: string): Promise<KillSwitch> => {
    const response = await api.post(`/admin/killswitch/${flagKey}/activate`, { reason });
    return response.data;
  },

  deactivateKillSwitch: async (flagKey: string): Promise<KillSwitch> => {
    const response = await api.post(`/admin/killswitch/${flagKey}/deactivate`);
    return response.data;
  },

  getActiveKillSwitches: async (): Promise<KillSwitch[]> => {
    const response = await api.get('/admin/killswitch/active');
    return response.data;
  },
};

