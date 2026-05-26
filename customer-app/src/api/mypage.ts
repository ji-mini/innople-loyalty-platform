import { apiClient } from './client';

export type LoginHistoryItem = {
  id: string;
  loginId: string;
  deviceName: string | null;
  osName: string | null;
  ip: string | null;
  userAgent: string | null;
  createdAt: string;
};

export async function getMyLoginHistories(limit = 5): Promise<LoginHistoryItem[]> {
  const { data } = await apiClient.get<LoginHistoryItem[]>('/api/v1/members/me/login-histories', {
    params: { limit },
  });
  return data;
}
