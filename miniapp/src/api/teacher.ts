import { request, apiBaseUrl, authHeaders } from '@/api/http';
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
  TodaySchedule,
  MaterialCategory,
  TeacherFileResult,
  TeacherMaterialItem,
  CreateMaterialRequest,
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

export function getTeacherSchedules(startDate?: string, endDate?: string) {
  const query = [startDate ? `startDate=${startDate}` : '', endDate ? `endDate=${endDate}` : '']
    .filter(Boolean)
    .join('&');
  return request<TodaySchedule[]>({
    url: `/api/teacher/schedules${query ? `?${query}` : ''}`,
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

export function uploadTeacherFile(filePath: string, fileName: string): Promise<TeacherFileResult> {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${apiBaseUrl()}/api/teacher/files`,
      filePath,
      name: 'file',
      header: authHeaders(),
      success: (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error('上传失败'));
          return;
        }
        try {
          const data = JSON.parse(res.data);
          if (data.code !== 'SUCCESS') {
            reject(new Error(data.message || '上传失败'));
            return;
          }
          resolve(data.data as TeacherFileResult);
        } catch {
          reject(new Error('解析响应失败'));
        }
      },
      fail: (err) => reject(new Error(err.errMsg || '上传失败')),
    });
  });
}

export function getTeacherMaterialCategories() {
  return request<MaterialCategory[]>({
    url: '/api/teacher/material-categories',
    method: 'GET',
  });
}

export function getTeacherMaterials(auditStatus?: string) {
  const query = auditStatus ? `?auditStatus=${encodeURIComponent(auditStatus)}` : '';
  return request<TeacherMaterialItem[]>({
    url: `/api/teacher/materials${query}`,
    method: 'GET',
  });
}

export function getTeacherMaterialDetail(id: number) {
  return request<TeacherMaterialItem>({
    url: `/api/teacher/materials/${id}`,
    method: 'GET',
  });
}

export function createTeacherMaterial(data: CreateMaterialRequest) {
  return request<TeacherMaterialItem>({
    url: '/api/teacher/materials',
    method: 'POST',
    data,
  });
}

export function deleteTeacherMaterial(id: number) {
  return request<void>({
    url: `/api/teacher/materials/${id}`,
    method: 'DELETE',
  });
}
