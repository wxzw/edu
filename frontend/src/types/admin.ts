import type { PageQuery } from './api';

export interface CampusRecord {
  id: number;
  code: string;
  name: string;
  shortName?: string;
  contactName?: string;
  contactPhone?: string;
  address?: string;
  businessHours?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export type CampusForm = Omit<CampusRecord, 'id' | 'createdAt' | 'updatedAt'>;

export interface UserRecord {
  id: number;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  accountType: string;
  status: string;
  roleIds: number[];
  campusIds: number[];
  lastLoginAt?: string;
  createdAt?: string;
}

export interface UserForm {
  username: string;
  password?: string;
  realName: string;
  phone?: string;
  email?: string;
  accountType: string;
  status: string;
  roleIds: number[];
  campusIds: number[];
  defaultCampusId?: number;
}

export interface RoleRecord {
  id: number;
  campusId?: number;
  code: string;
  name: string;
  scopeType: string;
  dataScope: string;
  status: string;
  remark?: string;
  permissionIds: number[];
}

export interface RoleForm {
  campusId?: number;
  code: string;
  name: string;
  scopeType: string;
  dataScope: string;
  status: string;
  remark?: string;
  permissionIds: number[];
}

export interface PermissionNode {
  id: number;
  parentId?: number;
  code: string;
  name: string;
  permissionType: string;
  routePath?: string;
  icon?: string;
  sortOrder?: number;
  children: PermissionNode[];
}

export interface TeacherRecord {
  id: number;
  campusId: number;
  userId?: number;
  employeeNo: string;
  name: string;
  gender?: string;
  phone?: string;
  title?: string;
  intro?: string;
  hireDate?: string;
  status: string;
}

export interface TeacherForm {
  userId?: number;
  employeeNo: string;
  name: string;
  gender?: string;
  phone?: string;
  title?: string;
  intro?: string;
  hireDate?: string;
  status: string;
}

export interface CourseRecord {
  id: number;
  campusId: number;
  courseCode: string;
  courseSystem: string;
  name: string;
  levelName?: string;
  targetAgeMin?: number;
  targetAgeMax?: number;
  gradeScope?: string;
  totalHours: number;
  unitPrice: number;
  packagePrice: number;
  description?: string;
  status: string;
}

export interface CourseForm {
  courseCode: string;
  courseSystem: string;
  name: string;
  levelName?: string;
  targetAgeMin?: number;
  targetAgeMax?: number;
  gradeScope?: string;
  totalHours: number;
  unitPrice: number;
  packagePrice: number;
  description?: string;
  status: string;
}

export interface ClassRecord {
  id: number;
  campusId: number;
  courseId: number;
  classNo: string;
  name: string;
  headTeacherId?: number;
  classroom?: string;
  classWechatQrUrl?: string;
  startDate?: string;
  endDate?: string;
  maxStudents: number;
  currentStudents: number;
  status: string;
  remark?: string;
}

export interface ClassForm {
  courseId?: number;
  classNo: string;
  name: string;
  headTeacherId?: number;
  classroom?: string;
  classWechatQrUrl?: string;
  startDate?: string;
  endDate?: string;
  maxStudents: number;
  status: string;
  remark?: string;
}

export interface ClassStudentRecord {
  classStudentId: number;
  studentId: number;
  studentNo: string;
  name: string;
  nickname?: string;
  gender?: string;
  birthday?: string;
  grade?: string;
  school?: string;
  englishLevel?: string;
  studentStatus: string;
  joinDate?: string;
  leaveDate?: string;
  classStudentStatus: string;
}

export interface StudentRecord {
  id: number;
  campusId: number;
  userId?: number;
  studentNo: string;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  gender?: string;
  birthday?: string;
  grade?: string;
  school?: string;
  englishLevel?: string;
  learningGoal?: string;
  status: string;
  enrolledAt?: string;
}

export interface StudentForm {
  userId?: number;
  studentNo: string;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  gender?: string;
  birthday?: string;
  grade?: string;
  school?: string;
  englishLevel?: string;
  learningGoal?: string;
  status: string;
  enrolledAt?: string;
}

export interface AdminPageQuery extends PageQuery {
  accountType?: string;
  courseSystem?: string;
  courseId?: number;
}
