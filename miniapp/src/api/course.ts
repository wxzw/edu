import { request } from '@/api/http';
import type {
  CourseRegistrationItem,
  CourseRegistrationResult,
  CreateCourseRegistrationRequest,
  MockPayResponse,
  PublicCampus,
  PublicCourseDetail,
  PublicCourseSummary,
} from '@/types/api';

export function getPublicCampuses() {
  return request<PublicCampus[]>({
    url: '/api/miniapp/public/campuses',
    method: 'GET',
  });
}

export function getPublicCourses(params: { campusId: number; keyword?: string }) {
  const query = [
    `campusId=${params.campusId}`,
    params.keyword ? `keyword=${encodeURIComponent(params.keyword)}` : '',
  ].filter(Boolean).join('&');
  return request<PublicCourseSummary[]>({
    url: `/api/miniapp/public/courses?${query}`,
    method: 'GET',
  });
}

export function getPublicCourseDetail(id: number, campusId: number) {
  return request<PublicCourseDetail>({
    url: `/api/miniapp/public/courses/${id}?campusId=${campusId}`,
    method: 'GET',
  });
}

export function createCourseRegistration(data: CreateCourseRegistrationRequest) {
  return request<CourseRegistrationResult>({
    url: '/api/miniapp/course-registrations',
    method: 'POST',
    data,
  });
}

export function getCourseRegistrations() {
  return request<CourseRegistrationItem[]>({
    url: '/api/miniapp/course-registrations/my',
    method: 'GET',
  });
}

export function payCourseOrder(id: number) {
  return request<MockPayResponse>({
    url: `/api/miniapp/orders/${id}/pay`,
    method: 'POST',
  });
}
