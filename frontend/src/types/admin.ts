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
  categoryId?: number;
  keyword?: string;
  resourceType?: string;
  studyType?: string;
  visibility?: string;
  status?: string;
  orderType?: string;
  payStatus?: string;
  studentId?: number;
  activityId?: number;
  orderId?: number;
  bizType?: string;
  receiverStudentId?: number;
  startDate?: string;
  endDate?: string;
}

export interface AdminFileInfo {
  id: number;
  storageType: string;
  objectKey: string;
  url: string;
  fileName: string;
  contentType?: string;
  fileSize: number;
  bizType?: string;
  status: string;
  createdAt?: string;
}

export interface MaterialCategoryRecord {
  id: number;
  parentId?: number;
  categoryType: string;
  name: string;
  sortOrder: number;
  status: string;
}

export interface MaterialCategoryForm {
  parentId?: number;
  name: string;
  sortOrder: number;
  status: string;
}

export interface MaterialRecord {
  id: number;
  categoryId: number;
  categoryName?: string;
  title: string;
  description?: string;
  resourceType: string;
  coverFileId?: number;
  coverUrl?: string;
  fileId: number;
  fileName?: string;
  ownerTeacherId?: number;
  ownerTeacherName?: string;
  visibility: string;
  studyType: string;
  allowDownload: boolean;
  auditStatus: string;
  status: string;
  classIds: number[];
  createdAt?: string;
  updatedAt?: string;
}

export interface MaterialForm {
  categoryId?: number;
  title: string;
  description?: string;
  resourceType: string;
  coverFileId?: number;
  fileId?: number;
  ownerTeacherId?: number;
  visibility: string;
  studyType: string;
  allowDownload: boolean;
  status: string;
  classIds: number[];
}

export interface ActivityRecord {
  id: number;
  title: string;
  description?: string;
  coverFileId?: number;
  coverUrl?: string;
  startTime: string;
  endTime: string;
  location: string;
  fee: number;
  quota?: number;
  registeredCount: number;
  status: string;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ActivityForm {
  title: string;
  description?: string;
  coverFileId?: number;
  startTime: string;
  endTime: string;
  location: string;
  fee: number;
  quota?: number;
  status: string;
}

export interface ActivityRegistrationRecord {
  id: number;
  activityId: number;
  activityTitle: string;
  studentId: number;
  studentName: string;
  guardianId?: number;
  orderId?: number;
  registrationNo: string;
  amount: number;
  status: string;
  registeredAt: string;
  payStatus?: string;
}

export interface OrderRecord {
  id: number;
  orderNo: string;
  orderType: string;
  studentId?: number;
  studentName?: string;
  guardianId?: number;
  activityId?: number;
  activityTitle?: string;
  totalAmount: number;
  discountAmount: number;
  paidAmount: number;
  payStatus: string;
  payChannel?: string;
  transactionNo?: string;
  payTime?: string;
  status: string;
  createdAt?: string;
}

export interface PaymentRecord {
  id: number;
  orderId: number;
  orderNo: string;
  paymentNo: string;
  payChannel: string;
  amount: number;
  transactionNo?: string;
  status: string;
  paidAt?: string;
  createdAt?: string;
}

export interface NotificationRecord {
  id: number;
  receiverUserId?: number;
  receiverStudentId?: number;
  receiverStudentName?: string;
  bizType: string;
  bizId?: number;
  title: string;
  content?: string;
  status: string;
  readAt?: string;
  createdAt?: string;
}

export interface NotificationForm {
  targetType: string;
  classId?: number;
  studentId?: number;
  title: string;
  content?: string;
  bizType?: string;
  bizId?: number;
}
