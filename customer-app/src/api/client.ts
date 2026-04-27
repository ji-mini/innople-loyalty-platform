import axios from 'axios';
import { Platform } from 'react-native';

const defaultBaseUrl =
  Platform.select({
    android: 'http://10.0.2.2:3201',
    ios: 'http://localhost:3201',
    default: 'http://localhost:3201',
  }) ?? 'http://localhost:3201';

export const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ?? defaultBaseUrl;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});
