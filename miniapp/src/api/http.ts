import { useAuthStore } from '@/stores/auth';
import type { ApiResponse } from '@/types/api';

const DEFAULT_BASE_URL = 'http://localhost:8055';

export function apiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || DEFAULT_BASE_URL;
}

export function authHeaders() {
  const auth = useAuthStore();
  const headers: Record<string, string> = {};
  if (auth.accessToken) {
    headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  if (auth.selectedIdentity?.campusId) {
    headers['X-Campus-Id'] = String(auth.selectedIdentity.campusId);
    headers['X-Identity-Type'] = auth.selectedIdentity.identityType;
    headers['X-Identity-Id'] = String(auth.selectedIdentity.identityId);
    if (auth.currentStudentId) {
      headers['X-Student-Id'] = String(auth.currentStudentId);
    }
  } else if (auth.userInfo?.defaultCampusId) {
    headers['X-Campus-Id'] = String(auth.userInfo.defaultCampusId);
  }
  return headers;
}

export function request<T>(options: UniApp.RequestOptions): Promise<T> {
  const headers: Record<string, string> = {
    ...(options.header as Record<string, string> | undefined),
    ...authHeaders(),
  };
  const auth = useAuthStore();

  return new Promise<T>((resolve, reject) => {
    uni.request({
      ...options,
      url: `${apiBaseUrl()}${options.url}`,
      header: headers,
      success: (response) => {
        const payload = response.data as ApiResponse<T>;
        if (response.statusCode === 401) {
          auth.clear();
          uni.reLaunch({ url: '/pages/login/index' });
          reject(new Error(payload?.message || '登录已过期'));
          return;
        }
        if (response.statusCode < 200 || response.statusCode >= 300 || payload?.code !== 'SUCCESS') {
          reject(new Error(payload?.message || '请求失败'));
          return;
        }
        resolve(payload.data);
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络异常'));
      },
    });
  });
}
