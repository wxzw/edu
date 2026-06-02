import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { loginApi, logoutApi } from '@/api/auth';
import type { CampusOption, LoginRequest, UserInfo } from '@/types/api';

const ACCESS_TOKEN_KEY = 'edu_admin_access_token';
const REFRESH_TOKEN_KEY = 'edu_admin_refresh_token';
const USER_INFO_KEY = 'edu_admin_user_info';
const CAMPUS_ID_KEY = 'edu_admin_campus_id';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem(ACCESS_TOKEN_KEY) || '');
  const refreshToken = ref(localStorage.getItem(REFRESH_TOKEN_KEY) || '');
  const userInfo = ref<UserInfo | null>(readJson<UserInfo>(USER_INFO_KEY));
  const selectedCampusId = ref<number | undefined>(readNumber(CAMPUS_ID_KEY) || userInfo.value?.defaultCampusId);

  const campusList = computed<CampusOption[]>(() => userInfo.value?.campusList || []);
  const permissions = computed<string[]>(() => userInfo.value?.permissions || []);
  const isLoggedIn = computed(() => Boolean(accessToken.value));

  const login = async (payload: LoginRequest) => {
    const response = await loginApi(payload);
    accessToken.value = response.accessToken;
    refreshToken.value = response.refreshToken;
    userInfo.value = response.userInfo;
    selectedCampusId.value = response.userInfo.defaultCampusId || response.userInfo.campusList[0]?.id;
    persist();
  };

  const switchCampus = (campusId: number) => {
    selectedCampusId.value = campusId;
    localStorage.setItem(CAMPUS_ID_KEY, String(campusId));
  };

  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission) || userInfo.value?.roles?.includes('SUPER_ADMIN');
  };

  const logout = async () => {
    const token = refreshToken.value;
    clearSession();
    if (token) {
      await logoutApi(token).catch(() => undefined);
    }
  };

  const clearSession = () => {
    accessToken.value = '';
    refreshToken.value = '';
    userInfo.value = null;
    selectedCampusId.value = undefined;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);
    localStorage.removeItem(CAMPUS_ID_KEY);
  };

  const persist = () => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken.value);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken.value);
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value));
    if (selectedCampusId.value) {
      localStorage.setItem(CAMPUS_ID_KEY, String(selectedCampusId.value));
    }
  };

  return {
    accessToken,
    refreshToken,
    userInfo,
    selectedCampusId,
    campusList,
    permissions,
    isLoggedIn,
    login,
    logout,
    clearSession,
    switchCampus,
    hasPermission,
  };
});

function readJson<T>(key: string): T | null {
  const value = localStorage.getItem(key);
  if (!value) return null;
  try {
    return JSON.parse(value) as T;
  } catch {
    return null;
  }
}

function readNumber(key: string): number | undefined {
  const value = localStorage.getItem(key);
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}
