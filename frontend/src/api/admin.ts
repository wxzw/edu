import { http } from './http';
import type { PageResponse, StatusUpdateRequest } from '@/types/api';
import type {
  AdminPageQuery,
  ActivityForm,
  ActivityRecord,
  ActivityRegistrationRecord,
  AdminFileInfo,
  CampusForm,
  CampusRecord,
  ClassForm,
  ClassRecord,
  ClassStudentRecord,
  CourseForm,
  CourseRecord,
  MaterialCategoryForm,
  MaterialCategoryRecord,
  MaterialForm,
  MaterialRecord,
  NotificationForm,
  NotificationRecord,
  OrderRecord,
  PaymentRecord,
  PermissionNode,
  RoleForm,
  RoleRecord,
  StudentForm,
  StudentRecord,
  TeacherForm,
  TeacherRecord,
  UserForm,
  UserRecord,
} from '@/types/admin';

const page = <T>(url: string, params: AdminPageQuery) => http.get<PageResponse<T>, PageResponse<T>>(url, { params });
const detail = <T>(url: string, id: number) => http.get<T, T>(`${url}/${id}`);
const create = <T, F>(url: string, payload: F) => http.post<T, T>(url, payload);
const update = <T, F>(url: string, id: number, payload: F) => http.put<T, T>(`${url}/${id}`, payload);
const status = (url: string, id: number, payload: StatusUpdateRequest) => http.patch<void, void>(`${url}/${id}/status`, payload);

export const campusApi = {
  page: (params: AdminPageQuery) => page<CampusRecord>('/api/admin/campuses', params),
  detail: (id: number) => detail<CampusRecord>('/api/admin/campuses', id),
  create: (payload: CampusForm) => create<CampusRecord, CampusForm>('/api/admin/campuses', payload),
  update: (id: number, payload: CampusForm) => update<CampusRecord, CampusForm>('/api/admin/campuses', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/campuses', id, payload),
};

export const userApi = {
  page: (params: AdminPageQuery) => page<UserRecord>('/api/admin/users', params),
  detail: (id: number) => detail<UserRecord>('/api/admin/users', id),
  create: (payload: UserForm) => create<UserRecord, UserForm>('/api/admin/users', payload),
  update: (id: number, payload: UserForm) => update<UserRecord, UserForm>('/api/admin/users', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/users', id, payload),
  resetPassword: (id: number, newPassword: string) =>
    http.patch<void, void>(`/api/admin/users/${id}/password`, { newPassword }),
};

export const roleApi = {
  page: (params: AdminPageQuery) => page<RoleRecord>('/api/admin/roles', params),
  detail: (id: number) => detail<RoleRecord>('/api/admin/roles', id),
  create: (payload: RoleForm) => create<RoleRecord, RoleForm>('/api/admin/roles', payload),
  update: (id: number, payload: RoleForm) => update<RoleRecord, RoleForm>('/api/admin/roles', id, payload),
  grant: (id: number, permissionIds: number[]) =>
    http.patch<void, void>(`/api/admin/roles/${id}/permissions`, { permissionIds }),
  permissionTree: () => http.get<PermissionNode[], PermissionNode[]>('/api/admin/roles/permission-tree'),
};

export const teacherApi = {
  page: (params: AdminPageQuery) => page<TeacherRecord>('/api/admin/teachers', params),
  detail: (id: number) => detail<TeacherRecord>('/api/admin/teachers', id),
  create: (payload: TeacherForm) => create<TeacherRecord, TeacherForm>('/api/admin/teachers', payload),
  update: (id: number, payload: TeacherForm) => update<TeacherRecord, TeacherForm>('/api/admin/teachers', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/teachers', id, payload),
};

export const courseApi = {
  page: (params: AdminPageQuery) => page<CourseRecord>('/api/admin/courses', params),
  detail: (id: number) => detail<CourseRecord>('/api/admin/courses', id),
  create: (payload: CourseForm) => create<CourseRecord, CourseForm>('/api/admin/courses', payload),
  update: (id: number, payload: CourseForm) => update<CourseRecord, CourseForm>('/api/admin/courses', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/courses', id, payload),
};

export const classApi = {
  page: (params: AdminPageQuery) => page<ClassRecord>('/api/admin/classes', params),
  detail: (id: number) => detail<ClassRecord>('/api/admin/classes', id),
  create: (payload: ClassForm) => create<ClassRecord, ClassForm>('/api/admin/classes', payload),
  update: (id: number, payload: ClassForm) => update<ClassRecord, ClassForm>('/api/admin/classes', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/classes', id, payload),
  students: (id: number) => http.get<ClassStudentRecord[], ClassStudentRecord[]>(`/api/admin/classes/${id}/students`),
  addStudent: (id: number, payload: { studentId: number; joinDate?: string }) =>
    http.post<ClassStudentRecord, ClassStudentRecord>(`/api/admin/classes/${id}/students`, payload),
  removeStudent: (id: number, studentId: number) =>
    http.delete<void, void>(`/api/admin/classes/${id}/students/${studentId}`),
};

export const studentApi = {
  page: (params: AdminPageQuery) => page<StudentRecord>('/api/admin/students', params),
  detail: (id: number) => detail<StudentRecord>('/api/admin/students', id),
  create: (payload: StudentForm) => create<StudentRecord, StudentForm>('/api/admin/students', payload),
  update: (id: number, payload: StudentForm) => update<StudentRecord, StudentForm>('/api/admin/students', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/students', id, payload),
};

export const fileApi = {
  uploadLocal: (file: File, bizType = 'MATERIAL') => {
    const form = new FormData();
    form.append('file', file);
    form.append('bizType', bizType);
    return http.post<AdminFileInfo, AdminFileInfo>('/api/admin/files/local', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const materialCategoryApi = {
  list: () => http.get<MaterialCategoryRecord[], MaterialCategoryRecord[]>('/api/admin/material-categories'),
  create: (payload: MaterialCategoryForm) =>
    create<MaterialCategoryRecord, MaterialCategoryForm>('/api/admin/material-categories', payload),
  update: (id: number, payload: MaterialCategoryForm) =>
    update<MaterialCategoryRecord, MaterialCategoryForm>('/api/admin/material-categories', id, payload),
};

export const materialApi = {
  page: (params: AdminPageQuery) => page<MaterialRecord>('/api/admin/materials', params),
  detail: (id: number) => detail<MaterialRecord>('/api/admin/materials', id),
  create: (payload: MaterialForm) => create<MaterialRecord, MaterialForm>('/api/admin/materials', payload),
  update: (id: number, payload: MaterialForm) => update<MaterialRecord, MaterialForm>('/api/admin/materials', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/materials', id, payload),
};

export const activityApi = {
  page: (params: AdminPageQuery) => page<ActivityRecord>('/api/admin/activities', params),
  detail: (id: number) => detail<ActivityRecord>('/api/admin/activities', id),
  create: (payload: ActivityForm) => create<ActivityRecord, ActivityForm>('/api/admin/activities', payload),
  update: (id: number, payload: ActivityForm) => update<ActivityRecord, ActivityForm>('/api/admin/activities', id, payload),
  status: (id: number, payload: StatusUpdateRequest) => status('/api/admin/activities', id, payload),
  registrations: (params: AdminPageQuery) => page<ActivityRegistrationRecord>('/api/admin/activity-registrations', params),
};

export const orderApi = {
  page: (params: AdminPageQuery) => page<OrderRecord>('/api/admin/orders', params),
};

export const paymentApi = {
  page: (params: AdminPageQuery) => page<PaymentRecord>('/api/admin/payments', params),
};

export const notificationApi = {
  page: (params: AdminPageQuery) => page<NotificationRecord>('/api/admin/notifications', params),
  publish: (payload: NotificationForm) =>
    http.post<{ sentCount: number }, { sentCount: number }>('/api/admin/notifications', payload),
};
