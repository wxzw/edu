export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  traceId?: string;
}

export interface CampusOption {
  id: number;
  code: string;
  name: string;
  shortName?: string;
  isDefault: boolean;
}

export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  phone?: string;
  accountType: IdentityType;
  roles: string[];
  permissions: string[];
  campusList: CampusOption[];
  defaultCampusId?: number;
}

export type IdentityType = 'TEACHER' | 'STUDENT' | 'GUARDIAN';

export interface ChildStudent {
  studentId: number;
  campusId: number;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  grade?: string;
  school?: string;
  relation?: string;
}

export interface MiniappIdentity {
  identityType: IdentityType;
  identityId: number;
  campusId: number;
  displayName: string;
  roleName: string;
  avatarUrl?: string;
  phone?: string;
  status: string;
  children?: ChildStudent[];
}

export interface MiniappLoginRequest {
  loginCode?: string;
  phoneCode?: string;
  roleHint?: IdentityType;
  mockOpenId?: string;
  mockUnionId?: string;
  mockPhone?: string;
}

export interface MiniappLoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  userInfo: UserInfo;
  availableIdentities: MiniappIdentity[];
  selectedIdentity: MiniappIdentity;
}

export interface MiniappMeResponse {
  userInfo: UserInfo;
  availableIdentities: MiniappIdentity[];
  selectedIdentity: MiniappIdentity;
}

export interface StudentProfile {
  studentId: number;
  campusId: number;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  grade?: string;
  school?: string;
  englishLevel?: string;
  learningGoal?: string;
  campusName?: string;
  campusShortName?: string;
  classNames?: string;
}

export interface LessonAccount {
  id: number;
  courseId: number;
  courseName: string;
  courseSystem?: string;
  purchasedHours: number;
  giftHours: number;
  adjustedHours: number;
  consumedHours: number;
  refundedHours: number;
  lockedHours: number;
  remainingHours: number;
  status: string;
}

export interface LessonRecord {
  id: number;
  courseId: number;
  courseName: string;
  className?: string;
  scheduleId?: number;
  lessonTopic?: string;
  changeType: string;
  hoursDelta: number;
  balanceAfter: number;
  occurredAt: string;
  remark?: string;
  attendanceStatus?: string;
}

export interface LessonSummary {
  totalRemainingHours: number;
  lowBalance: boolean;
  accounts: LessonAccount[];
}

export interface ScheduleItem {
  id: number;
  classId: number;
  className: string;
  courseId: number;
  courseName: string;
  teacherName: string;
  lessonNo?: number;
  lessonDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  content?: string;
  lessonHours: number;
  status: string;
  classroom?: string;
  onlineUrl?: string;
}

export interface CalendarScheduleItem {
  id: number;
  classId?: number;
  className?: string;
  courseId?: number;
  courseName?: string;
  teacherName?: string;
  lessonNo?: number;
  lessonDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  content?: string;
  lessonHours?: number;
  status: string;
  classroom?: string;
  onlineUrl?: string;
  studentCount?: number;
  attendanceCount?: number;
}

export interface TodoItem {
  id?: number;
  bizType: string;
  bizId?: number;
  title: string;
  content?: string;
  status: string;
  createdAt?: string;
}

export interface GroupRequestSummary {
  id: number;
  requestNo: string;
  childAge?: number;
  grade?: string;
  targetSystem: string;
  englishLevel?: string;
  preferredTimes: string[];
  remark?: string;
  requiredMembers: number;
  currentMembers: number;
  status: string;
  shareCode: string;
  posterUrl?: string;
  expiresAt?: string;
  createdAt?: string;
}

export interface GroupMember {
  id: number;
  studentId?: number;
  guardianId?: number;
  nickname: string;
  avatarUrl?: string;
  joinStatus: string;
  joinedAt: string;
}

export interface GroupTrial {
  id: number;
  trialTime: string;
  location: string;
  teacherName?: string;
  className?: string;
  wechatQrUrl?: string;
  status: string;
  remark?: string;
}

export interface GroupTrialFeedback {
  id: number;
  memberId?: number;
  studentId?: number;
  feedback: string;
  result: string;
  nextAction?: string;
  createdAt?: string;
}

export interface GroupRequestDetail extends GroupRequestSummary {
  members: GroupMember[];
  trial?: GroupTrial;
  feedbacks: GroupTrialFeedback[];
  sharePath: string;
}

export interface GroupPoster {
  groupRequestId: number;
  posterUrl?: string;
  shareCode: string;
  sharePath: string;
}

export interface StudentDashboard {
  profile: StudentProfile;
  children: ChildStudent[];
  currentStudentId: number;
  lessonSummary: LessonSummary;
  nextLesson?: ScheduleItem;
  todos: TodoItem[];
  activeGroupRequest?: GroupRequestSummary;
}

export interface HomeworkListItem {
  id: number;
  title: string;
  content: string;
  teacherName: string;
  className?: string;
  deadline?: string;
  checkinEnabled?: boolean;
  publishedAt?: string;
  submissionId?: number;
  submissionStatus?: string;
  submittedAt?: string;
  studentStatus: 'TO_SUBMIT' | 'OVERDUE' | 'SUBMITTED' | 'COMMENTED' | string;
  attachmentCount?: number;
}

export interface FileItem {
  fileId: number;
  fileName: string;
  url: string;
  contentType?: string;
  fileSize?: number;
  mediaType?: string;
  sortOrder?: number;
}

export interface HomeworkSubmission {
  id: number;
  content?: string;
  status: string;
  submittedAt?: string;
}

export interface HomeworkComment {
  id: number;
  teacherName?: string;
  commentText?: string;
  voiceFileId?: number;
  voiceUrl?: string;
  rating?: number;
  commentedAt?: string;
}

export interface HomeworkDetail extends HomeworkListItem {
  attachments: FileItem[];
  submission?: HomeworkSubmission;
  submissionFiles: FileItem[];
  comments: HomeworkComment[];
}

export interface HomeworkSubmitRequest {
  content?: string;
  files?: Array<{
    fileId: number;
    mediaType: string;
    sortOrder?: number;
  }>;
}

export interface GroupCreateRequest {
  childAge: number;
  grade: string;
  targetSystem: string;
  englishLevel: string;
  preferredTimes: string[];
  remark?: string;
  contactPhone?: string;
}

export interface MaterialCategory {
  id: number;
  parentId?: number;
  name: string;
  sortOrder?: number;
}

export interface MaterialSummary {
  id: number;
  categoryId: number;
  categoryName?: string;
  title: string;
  description?: string;
  resourceType: string;
  studyType: 'REQUIRED' | 'OPTIONAL' | string;
  allowDownload: boolean;
  fileId: number;
  fileName?: string;
  fileUrl?: string;
  contentType?: string;
  fileSize?: number;
  coverUrl?: string;
  previewPath: string;
  downloadPath: string;
  updatedAt?: string;
}

export interface TeacherFileResult {
  fileId: number;
  fileName: string;
  contentType: string;
  fileSize: number;
  objectKey: string;
}

export interface TeacherMaterialItem {
  id: number;
  title: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  resourceType: string;
  visibility: string;
  studyType: string;
  allowDownload: boolean;
  auditStatus: string;
  status: string;
  fileId: number;
  fileName?: string;
  fileSize?: number;
  contentType?: string;
  coverUrl?: string;
  createdAt?: string;
  rejectedReason?: string;
}

export interface CreateMaterialRequest {
  title: string;
  description?: string;
  categoryId: number;
  resourceType: string;
  visibility: string;
  studyType: string;
  allowDownload: boolean;
  fileId: number;
  coverFileId?: number;
  classIds?: number[];
}

export interface ActivitySummary {
  id: number;
  title: string;
  description?: string;
  coverUrl?: string;
  startTime: string;
  endTime: string;
  location: string;
  fee: number;
  quota?: number;
  registeredCount: number;
  status: string;
  activityStatus: string;
  registrationId?: number;
  registrationStatus?: string;
  orderId?: number;
  payStatus?: string;
}

export interface ActivityDetail extends ActivitySummary {}

export interface JoinActivityResponse {
  activityId: number;
  registrationId?: number;
  orderId?: number;
  payRequired: boolean;
  amount: number;
  status: string;
  message?: string;
}

export interface RegistrationItem {
  id: number;
  activityId: number;
  orderId?: number;
  registrationNo: string;
  amount: number;
  status: string;
  registeredAt: string;
  activityTitle: string;
  startTime: string;
  endTime: string;
  location: string;
  payStatus?: string;
}

export interface OrderDetail {
  id: number;
  orderNo: string;
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
  remark?: string;
  createdAt?: string;
}

export interface MockPayResponse {
  orderId: number;
  registrationId?: number;
  payStatus: string;
  transactionNo?: string;
  paidAmount: number;
}

export interface NotificationItem {
  id: number;
  bizType: string;
  bizId?: number;
  title: string;
  content?: string;
  status: 'UNREAD' | 'READ' | string;
  readAt?: string;
  createdAt?: string;
}

export interface NotificationPage {
  records: NotificationItem[];
  unreadCount: number;
}

export interface TeacherProfile {
  teacherId: number;
  campusId: number;
  name: string;
  avatarUrl?: string;
  phone?: string;
  title?: string;
  campusName?: string;
  campusShortName?: string;
}

export interface TodaySchedule {
  id: number;
  classId: number;
  className: string;
  courseId: number;
  courseName: string;
  lessonNo?: number;
  lessonDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  classroom?: string;
  lessonHours: number;
  status: string;
  studentCount?: number;
  attendanceCount?: number;
}

export interface TodoStats {
  pendingAttendance: number;
  pendingComment: number;
  pendingAudit: number;
}

export interface QuickStats {
  classCount: number;
  studentCount: number;
  weekScheduleCount: number;
}

export interface TeacherDashboard {
  profile: TeacherProfile;
  todaySchedules: TodaySchedule[];
  todoStats: TodoStats;
  quickStats: QuickStats;
}

export interface ClassListItem {
  id: number;
  name: string;
  courseId: number;
  courseName: string;
  courseSystem?: string;
  studentCount?: number;
  status: string;
}

export interface ClassStudentItem {
  studentId: number;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  grade?: string;
  school?: string;
  remainingHours?: number;
  lastAttendanceStatus?: string;
}

export interface ClassDetail {
  id: number;
  name: string;
  courseId: number;
  courseName: string;
  courseSystem?: string;
  status: string;
  students: ClassStudentItem[];
}

export interface StudentLessonAccount {
  id: number;
  courseId: number;
  courseName: string;
  remainingHours: number;
}

export interface StudentLessonRecord {
  id: number;
  courseId: number;
  courseName: string;
  className?: string;
  lessonTopic?: string;
  changeType: string;
  hoursDelta: number;
  occurredAt: string;
  attendanceStatus?: string;
}

export interface AttendanceStats {
  totalClasses: number;
  presentCount: number;
  absentCount: number;
  attendanceRate: number;
}

export interface HomeworkStats {
  totalAssigned: number;
  submittedCount: number;
  commentedCount: number;
  completionRate: number;
}

export interface TeacherStudentProfile {
  studentId: number;
  campusId: number;
  name: string;
  nickname?: string;
  avatarUrl?: string;
  grade?: string;
  school?: string;
  englishLevel?: string;
  parentPhone?: string;
  campusName?: string;
  lessonAccounts: StudentLessonAccount[];
  lessonRecords: StudentLessonRecord[];
  attendanceStats: AttendanceStats;
  homeworkStats: HomeworkStats;
  classNames: string[];
}

export interface TeacherHomeworkListItem {
  id: number;
  title: string;
  content: string;
  className?: string;
  deadline?: string;
  checkinEnabled?: boolean;
  publishedAt?: string;
  status: string;
  totalSubmissions?: number;
  pendingSubmissions?: number;
  attachmentCount?: number;
}

export interface HomeworkSubmissionItem {
  id: number;
  studentId: number;
  studentName: string;
  studentAvatarUrl?: string;
  content?: string;
  status: string;
  submittedAt?: string;
  files?: FileItem[];
  comments?: HomeworkComment[];
}

export interface TeacherHomeworkDetail extends TeacherHomeworkListItem {
  attachments: FileItem[];
  submissions: HomeworkSubmissionItem[];
}

export interface CreateHomeworkRequest {
  title: string;
  content?: string;
  attachments?: Array<{
    fileId: number;
    mediaType: string;
    sortOrder?: number;
  }>;
  targetType: string;
  targetClassIds?: number[];
  targetStudentIds?: number[];
  deadline?: string;
  checkinEnabled?: boolean;
  checkinDays?: number;
}

export interface CommentHomeworkRequest {
  commentText?: string;
  voiceFileId?: number;
  rating?: number;
  status: string;
}

export interface AttendanceListItem {
  scheduleId: number;
  classId: number;
  className: string;
  lessonDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  studentCount?: number;
  attendanceCount?: number;
  status: string;
}

export interface AttendanceStudentItem {
  attendanceId?: number;
  studentId: number;
  studentName: string;
  studentAvatarUrl?: string;
  status: AttendanceStatus | 'UNSET' | string;
  remark?: string;
}

export type AttendanceStatus =
  | 'PRESENT'
  | 'LATE'
  | 'LEAVE_EARLY'
  | 'ABSENT'
  | 'SICK_LEAVE'
  | 'PERSONAL_LEAVE'
  | 'MAKEUP';

export interface AttendanceDetail {
  scheduleId: number;
  classId: number;
  className: string;
  courseId?: number;
  courseName?: string;
  lessonDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  classroom?: string;
  lessonHours: number;
  studentCount?: number;
  attendanceCount?: number;
  students: AttendanceStudentItem[];
}

export interface BatchAttendanceRequest {
  attendances: Array<{
    studentId: number;
    status: AttendanceStatus;
    remark?: string;
  }>;
}

export interface DeductLessonHoursRequest {
  scheduleId: number;
  studentIds?: number[];
}

export interface LessonHourRecordItem {
  id: number;
  studentId: number;
  studentName: string;
  courseId: number;
  courseName: string;
  hoursDelta: number;
  balanceAfter?: number;
  changeType: string;
  occurredAt: string;
}
