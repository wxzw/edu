import type { IdentityType } from '@/types/api';

const HOME_ROUTES: Record<IdentityType, string> = {
  TEACHER: '/pages/teacher/home',
  STUDENT: '/pages/student/home',
  GUARDIAN: '/pages/student/home',
};

export function homeRoute(identityType?: IdentityType) {
  return identityType ? HOME_ROUTES[identityType] : '/pages/login/index';
}

export function switchToHome(identityType?: IdentityType) {
  const url = homeRoute(identityType);
  // 使用 reLaunch 而非 switchTab，因为自定义 TabBar 不依赖原生 tabBar
  uni.reLaunch({ url });
}
