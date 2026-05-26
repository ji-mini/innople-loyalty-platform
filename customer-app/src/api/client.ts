import axios, { AxiosHeaders } from 'axios';
import { Platform } from 'react-native';

import { TENANT_ID } from '../config/app';
import { loadAccessToken } from '../utils/tokenStorage';

const defaultBaseUrl =
  Platform.select({
    android: 'http://10.0.2.2:3201',
    ios: 'http://localhost:3201',
    default: 'http://localhost:3201',
  }) ?? 'http://localhost:3201';

export const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ?? defaultBaseUrl;

let accessToken: string | null = loadAccessToken();

type HeaderMap = Record<string, string>;

function getNavigatorUserAgent() {
  if (typeof navigator === 'undefined') {
    return null;
  }
  const userAgent = navigator.userAgent;
  return typeof userAgent === 'string' && userAgent.trim().length > 0 ? userAgent.trim() : null;
}

function inferWebDeviceName(userAgent: string | null) {
  if (!userAgent) {
    return 'Web Browser';
  }
  const lowerUserAgent = userAgent.toLowerCase();
  if (lowerUserAgent.includes('ipad')) {
    return 'iPad';
  }
  if (lowerUserAgent.includes('iphone')) {
    return 'iPhone';
  }
  if (lowerUserAgent.includes('android')) {
    return lowerUserAgent.includes('mobile') ? 'Android Phone' : 'Android Tablet';
  }
  return 'Web Browser';
}

function inferWebOsName(userAgent: string | null) {
  if (!userAgent) {
    return 'Web';
  }
  const lowerUserAgent = userAgent.toLowerCase();
  if (lowerUserAgent.includes('iphone') || lowerUserAgent.includes('ipad') || lowerUserAgent.includes('cpu os')) {
    return 'iOS';
  }
  if (lowerUserAgent.includes('android')) {
    return 'Android';
  }
  if (lowerUserAgent.includes('windows')) {
    return 'Windows';
  }
  if (lowerUserAgent.includes('macintosh') || lowerUserAgent.includes('mac os x')) {
    return 'macOS';
  }
  if (lowerUserAgent.includes('linux')) {
    return 'Linux';
  }
  return 'Web';
}

function getClientMetadata() {
  const userAgent = getNavigatorUserAgent();

  if (Platform.OS === 'web') {
    return {
      deviceName: inferWebDeviceName(userAgent),
      osName: inferWebOsName(userAgent),
    };
  }

  if (Platform.OS === 'ios') {
    return {
      deviceName: 'iOS Device',
      osName: `iOS ${String(Platform.Version ?? '').trim()}`.trim(),
    };
  }

  if (Platform.OS === 'android') {
    return {
      deviceName: 'Android Device',
      osName: `Android ${String(Platform.Version ?? '').trim()}`.trim(),
    };
  }

  return {
    deviceName: Platform.OS,
    osName: Platform.OS,
  };
}

function normalizeHeaders(headers?: HeadersInit | HeaderMap) {
  if (!headers) {
    return {};
  }

  if (headers instanceof Headers) {
    return Object.fromEntries(headers.entries());
  }

  if (Array.isArray(headers)) {
    return Object.fromEntries(headers.map(([key, value]) => [key, String(value)]));
  }

  return Object.fromEntries(
    Object.entries(headers).map(([key, value]) => [key, String(value)]),
  );
}

export function buildApiHeaders(headers?: HeadersInit | HeaderMap) {
  const normalizedHeaders = normalizeHeaders(headers);
  const clientMetadata = getClientMetadata();

  if (TENANT_ID) {
    normalizedHeaders['X-Tenant-Id'] = TENANT_ID;
  } else {
    delete normalizedHeaders['X-Tenant-Id'];
  }

  if (accessToken) {
    normalizedHeaders.Authorization = `Bearer ${accessToken}`;
  } else {
    delete normalizedHeaders.Authorization;
  }

  if (!normalizedHeaders['Content-Type']) {
    normalizedHeaders['Content-Type'] = 'application/json';
  }

  normalizedHeaders['X-Client-Device'] = clientMetadata.deviceName;
  normalizedHeaders['X-Client-OS'] = clientMetadata.osName;

  return normalizedHeaders;
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: buildApiHeaders(),
});

export function setAccessToken(token: string | null) {
  accessToken = token;
}

apiClient.interceptors.request.use((config) => {
  config.headers = AxiosHeaders.from(buildApiHeaders(config.headers));
  return config;
});

export async function apiFetch(input: string, init: RequestInit = {}) {
  const headers = buildApiHeaders(init.headers);
  return fetch(`${API_BASE_URL}${input}`, {
    ...init,
    headers,
  });
}
