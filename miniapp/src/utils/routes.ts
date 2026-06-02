import type { IdentityType } from '@/types/api';

const HOME_ROUTES: Record<IdentityType, string> = {
  TEACHER: '/pages/teacher/home',
  STUDENT: '/pages/student/home',
  GUARDIAN: '/pages/guardian/home',
};

export function homeRoute(identityType?: IdentityType) {
  return identityType ? HOME_ROUTES[identityType] : '/pages/login/index';
}

export function switchToHome(identityType?: IdentityType) {
  uni.reLaunch({
    url: homeRoute(identityType),
  });
}
