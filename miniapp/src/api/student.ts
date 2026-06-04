import { request } from '@/api/http';
import type {
  GroupCreateRequest,
  GroupPoster,
  GroupRequestDetail,
  GroupRequestSummary,
  HomeworkDetail,
  HomeworkListItem,
  HomeworkSubmitRequest,
  ActivityDetail,
  ActivitySummary,
  JoinActivityResponse,
  LessonAccount,
  LessonRecord,
  MaterialCategory,
  MaterialSummary,
  MockPayResponse,
  NotificationPage,
  OrderDetail,
  RegistrationItem,
  ScheduleItem,
  StudentDashboard,
} from '@/types/api';

export function getStudentDashboard() {
  return request<StudentDashboard>({
    url: '/api/student/dashboard',
    method: 'GET',
  });
}

export function getHomeworks() {
  return request<HomeworkListItem[]>({
    url: '/api/student/homeworks',
    method: 'GET',
  });
}

export function getHomeworkDetail(id: number) {
  return request<HomeworkDetail>({
    url: `/api/student/homeworks/${id}`,
    method: 'GET',
  });
}

export function submitHomework(id: number, data: HomeworkSubmitRequest) {
  return request<HomeworkDetail>({
    url: `/api/student/homeworks/${id}/submit`,
    method: 'POST',
    data,
  });
}

export function getSchedules(startDate?: string, endDate?: string) {
  const query = [startDate ? `startDate=${startDate}` : '', endDate ? `endDate=${endDate}` : '']
    .filter(Boolean)
    .join('&');
  return request<ScheduleItem[]>({
    url: `/api/student/schedules${query ? `?${query}` : ''}`,
    method: 'GET',
  });
}

export function getLessonAccounts() {
  return request<LessonAccount[]>({
    url: '/api/student/lesson-hour-accounts',
    method: 'GET',
  });
}

export function getLessonRecords(limit = 50) {
  return request<LessonRecord[]>({
    url: `/api/student/lesson-hour-records?limit=${limit}`,
    method: 'GET',
  });
}

export function getGroupRequests() {
  return request<GroupRequestSummary[]>({
    url: '/api/student/group-requests',
    method: 'GET',
  });
}

export function createGroupRequest(data: GroupCreateRequest) {
  return request<GroupRequestDetail>({
    url: '/api/student/group-requests',
    method: 'POST',
    data,
  });
}

export function getGroupRequestDetail(id: number) {
  return request<GroupRequestDetail>({
    url: `/api/student/group-requests/${id}`,
    method: 'GET',
  });
}

export function getGroupRequestByShareCode(shareCode: string) {
  return request<GroupRequestDetail>({
    url: `/api/student/group-requests/by-share-code/${shareCode}`,
    method: 'GET',
  });
}

export function joinGroupRequest(id: number, data: { nickname?: string; phone?: string } = {}) {
  return request<GroupRequestDetail>({
    url: `/api/student/group-requests/${id}/join`,
    method: 'POST',
    data,
  });
}

export function createGroupPoster(id: number) {
  return request<GroupPoster>({
    url: `/api/student/group-requests/${id}/poster`,
    method: 'POST',
  });
}

export function getMaterialCategories() {
  return request<MaterialCategory[]>({
    url: '/api/student/material-categories',
    method: 'GET',
  });
}

export function getMaterials(params: { categoryId?: number; studyType?: string; resourceType?: string; keyword?: string } = {}) {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== '')
    .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
    .join('&');
  return request<MaterialSummary[]>({
    url: `/api/student/materials${query ? `?${query}` : ''}`,
    method: 'GET',
  });
}

export function getMaterialDetail(id: number) {
  return request<MaterialSummary>({
    url: `/api/student/materials/${id}`,
    method: 'GET',
  });
}

export function getActivities(keyword = '') {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
  return request<ActivitySummary[]>({
    url: `/api/student/activities${query}`,
    method: 'GET',
  });
}

export function getActivityDetail(id: number) {
  return request<ActivityDetail>({
    url: `/api/student/activities/${id}`,
    method: 'GET',
  });
}

export function joinActivity(id: number) {
  return request<JoinActivityResponse>({
    url: `/api/student/activities/${id}/join`,
    method: 'POST',
  });
}

export function getRegistrations() {
  return request<RegistrationItem[]>({
    url: '/api/student/registrations',
    method: 'GET',
  });
}

export function getOrderDetail(id: number) {
  return request<OrderDetail>({
    url: `/api/student/orders/${id}`,
    method: 'GET',
  });
}

export function payOrder(id: number) {
  return request<MockPayResponse>({
    url: `/api/student/orders/${id}/pay`,
    method: 'POST',
  });
}

export function getNotifications(status?: string) {
  return request<NotificationPage>({
    url: `/api/student/notifications${status ? `?status=${status}` : ''}`,
    method: 'GET',
  });
}

export function readNotification(id: number) {
  return request<void>({
    url: `/api/student/notifications/${id}/read`,
    method: 'POST',
  });
}

export function readAllNotifications() {
  return request<void>({
    url: '/api/student/notifications/read-all',
    method: 'POST',
  });
}
