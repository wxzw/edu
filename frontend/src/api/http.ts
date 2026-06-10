import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useAuthStore } from '@/stores/auth';
import type { ApiResponse } from '@/types/api';

export const http = axios.create({
  baseURL: '/',
  timeout: 15000,
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authStore = useAuthStore();
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  if (authStore.selectedCampusId) {
    config.headers['X-Campus-Id'] = String(authStore.selectedCampusId);
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response;
    }
    const payload = response.data as ApiResponse<unknown>;
    if (payload && payload.code && payload.code !== 'SUCCESS') {
      ElMessage.error(payload.message || '请求失败');
      return Promise.reject(new Error(payload.message));
    }
    return payload?.data ?? response.data;
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.message || error.message || '网络异常';
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.clearSession();
      if (router.currentRoute.value.name !== 'login') {
        await router.replace({ name: 'login' });
      }
    }
    ElMessage.error(message);
    return Promise.reject(error);
  },
);
