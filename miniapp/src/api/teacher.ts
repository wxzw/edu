import { request } from '@/api/http';
import type {
  TeacherDashboard,
  ClassListItem,
  ClassDetail,
  TeacherStudentProfile,
  TeacherHomeworkListItem,
  TeacherHomeworkDetail,
  CreateHomeworkRequest,
  CommentHomeworkRequest,
  AttendanceListItem,
  AttendanceDetail,
  BatchAttendanceRequest,
  DeductLessonHoursRequest,
  LessonHourRecordItem,
} from '@/types/api';

export function getTeacherDashboard() {
  return request<TeacherDashboard>({
    url: '/api/teacher/dashboard',
    method: 'GET',
  });
}

export function getTeacherClasses() {
  return request<ClassListItem[]>({
    url: '/api/teacher/classes',
    method: 'GET',
  });
}

export function getTeacherClassDetail(id: number) {
  return request<ClassDetail>({
    url: `/api/teacher/classes/${id}`,
    method: 'GET',
  });
}

export function getTeacherStudentProfile(id: number) {
  return request<TeacherStudentProfile>({
    url: `/api/teacher/students/${id}`,
    method: 'GET',
  });
}

export function getTeacherHomeworks() {
  return request<TeacherHomeworkListItem[]>({
    url: '/api/teacher/homeworks',
    method: 'GET',
  });
}

export function getTeacherHomeworkDetail(id: number) {
  return request<TeacherHomeworkDetail>({
    url: `/api/teacher/homeworks/${id}`,
    method: 'GET',
  });
}

export function createTeacherHomework(data: CreateHomeworkRequest) {
  return request<TeacherHomeworkDetail>({
    url: '/api/teacher/homeworks',
    method: 'POST',
    data,
  });
}

export function publishTeacherHomework(id: number) {
  return request<TeacherHomeworkDetail>({
    url: `/api/teacher/homeworks/${id}/publish`,
    method: 'POST',
  });
}

export function commentTeacherHomework(id: number, submissionId: number, data: CommentHomeworkRequest) {
  return request<TeacherHomeworkDetail>({
    url: `/api/teacher/homeworks/${id}/submissions/${submissionId}/comment`,
    method: 'POST',
    data,
  });
}

export function getTodaySchedulesForAttendance() {
  return request<AttendanceListItem[]>({
    url: '/api/teacher/schedules/today',
    method: 'GET',
  });
}

export function getAttendanceDetail(scheduleId: number) {
  return request<AttendanceDetail>({
    url: `/api/teacher/schedules/${scheduleId}/attendance`,
    method: 'GET',
  });
}

export function batchAttendance(scheduleId: number, data: BatchAttendanceRequest) {
  return request<void>({
    url: `/api/teacher/schedules/${scheduleId}/attendance`,
    method: 'POST',
    data,
  });
}

export function deductLessonHours(scheduleId: number, data: DeductLessonHoursRequest) {
  return request<void>({
    url: `/api/teacher/schedules/${scheduleId}/deduct`,
    method: 'POST',
    data,
  });
}

export function getTeacherLessonHourRecords(limit = 50) {
  return request<LessonHourRecordItem[]>({
    url: `/api/teacher/lesson-hour-records?limit=${limit}`,
    method: 'GET',
  });
}
