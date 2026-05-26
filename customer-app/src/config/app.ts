function readPublicEnv(name: 'EXPO_PUBLIC_TENANT_ID' | 'EXPO_PUBLIC_APP_NAME') {
  const value = process.env[name];
  return typeof value === 'string' && value.trim().length > 0 ? value.trim() : null;
}

export const TENANT_ID = readPublicEnv('EXPO_PUBLIC_TENANT_ID');

export const APP_NAME = readPublicEnv('EXPO_PUBLIC_APP_NAME') ?? 'INNOPLE LOYALTY';

export const CURRENT_APP_NAME = readPublicEnv('EXPO_PUBLIC_APP_NAME');

export function hasTenantConfig() {
  return Boolean(TENANT_ID);
}
