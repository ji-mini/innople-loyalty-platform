import { apiClient } from './client';
import { TENANT_ID } from '../config/app';

export type AuthMember = {
  memberId: string;
  memberNo: string;
  name: string;
  email: string | null;
  phone: string | null;
  grade: string | null;
  pointBalance: number;
};

export type AuthResponse = {
  accessToken: string;
  member: AuthMember;
};

export type SignupPayload = {
  name: string;
  email?: string;
  password: string;
  phoneNumber: string;
};

export async function signup(payload: SignupPayload): Promise<AuthResponse> {
  const requestBody = {
    tenantId: TENANT_ID,
    name: payload.name,
    password: payload.password,
    phoneNumber: payload.phoneNumber,
    phone: payload.phoneNumber,
    ...(payload.email ? { email: payload.email } : {}),
  };

  const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/signup', requestBody);
  return data;
}

export async function login(phoneNumber: string, password: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/login', {
    tenantId: TENANT_ID,
    phoneNumber,
    password,
  });
  return data;
}
