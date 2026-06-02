INSERT INTO sys_campus (
  id, code, name, short_name, contact_name, contact_phone, address, business_hours, status, settings, created_by, updated_by
) VALUES
  (1001, 'CAMPUS-SUNNY', '阳光花园社区校区', '阳光校区', '王园长', '13800000001', '阳光花园三期12栋社区中心2楼', '周一至周日 09:00-20:30', 'ENABLED', '{"timezone":"Asia/Shanghai","defaultLessonHours":2}'::JSONB, 1, 1),
  (1002, 'CAMPUS-RIVER', '滨河家园社区校区', '滨河校区', '李园长', '13800000002', '滨河家园北门商业街208号', '周二至周日 10:00-20:00', 'ENABLED', '{"timezone":"Asia/Shanghai","defaultLessonHours":2}'::JSONB, 1, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 1, 'admin', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), '超级管理员', '13900000000', 'SUPER_ADMIN', 'ENABLED', 1, 1
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 2, 'campus_admin', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), '阳光校区管理员', '13900000001', 'CAMPUS_ADMIN', 'ENABLED', 1, 1
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 3, 'teacher_amy', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), 'Amy老师', '13900000011', 'TEACHER', 'ENABLED', 2, 2
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 4, 'teacher_bob', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), 'Bob老师', '13900000012', 'TEACHER', 'ENABLED', 2, 2
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 5, 'parent_li', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), '李女士', '13900000101', 'GUARDIAN', 'ENABLED', 2, 2
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (
  id, username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 6, 'parent_wang', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), '王先生', '13900000102', 'GUARDIAN', 'ENABLED', 2, 2
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_role (
  id, campus_id, code, name, scope_type, data_scope, status, remark, created_by, updated_by
) VALUES
  (1, NULL, 'SUPER_ADMIN', '超级管理员', 'SYSTEM', 'ALL', 'ENABLED', '平台最高权限', 1, 1),
  (2, NULL, 'CAMPUS_ADMIN', '校区管理员', 'SYSTEM', 'CAMPUS', 'ENABLED', '校区运营管理权限', 1, 1),
  (3, NULL, 'TEACHER', '老师', 'SYSTEM', 'SELF', 'ENABLED', '老师端教学教务权限', 1, 1),
  (4, NULL, 'GUARDIAN', '学生/家长', 'SYSTEM', 'SELF', 'ENABLED', '学生端小程序权限', 1, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_permission (
  id, parent_id, code, name, permission_type, route_path, component, api_method, api_path, icon, sort_order, status, created_by, updated_by
) VALUES
  (100, NULL, 'dashboard', '工作台', 'MENU', '/dashboard', 'DashboardView', NULL, NULL, 'dashboard', 10, 'ENABLED', 1, 1),
  (110, NULL, 'system', '系统管理', 'MENU', '/system', 'Layout', NULL, NULL, 'setting', 20, 'ENABLED', 1, 1),
  (111, 110, 'system:campus', '校区管理', 'MENU', '/system/campus', 'system/CampusList', NULL, NULL, 'office-building', 21, 'ENABLED', 1, 1),
  (112, 110, 'system:user', '用户管理', 'MENU', '/system/user', 'system/UserList', NULL, NULL, 'user', 22, 'ENABLED', 1, 1),
  (113, 110, 'system:role', '角色权限', 'MENU', '/system/role', 'system/RoleList', NULL, NULL, 'lock', 23, 'ENABLED', 1, 1),
  (120, NULL, 'edu', '教务管理', 'MENU', '/edu', 'Layout', NULL, NULL, 'school', 30, 'ENABLED', 1, 1),
  (121, 120, 'edu:teacher', '老师管理', 'MENU', '/edu/teacher', 'edu/TeacherList', NULL, NULL, 'user-round-check', 31, 'ENABLED', 1, 1),
  (122, 120, 'edu:student', '学生管理', 'MENU', '/edu/student', 'edu/StudentList', NULL, NULL, 'users', 32, 'ENABLED', 1, 1),
  (123, 120, 'edu:course', '课程管理', 'MENU', '/edu/course', 'edu/CourseList', NULL, NULL, 'book-open', 33, 'ENABLED', 1, 1),
  (124, 120, 'edu:class', '班级管理', 'MENU', '/edu/class', 'edu/ClassList', NULL, NULL, 'book-user', 34, 'ENABLED', 1, 1),
  (125, 120, 'edu:schedule', '课表考勤', 'MENU', '/edu/schedule', 'edu/ScheduleList', NULL, NULL, 'calendar-days', 35, 'ENABLED', 1, 1),
  (126, 120, 'edu:homework', '作业管理', 'MENU', '/edu/homework', 'edu/HomeworkList', NULL, NULL, 'clipboard-check', 36, 'ENABLED', 1, 1),
  (127, 120, 'edu:lesson-hour', '课时管理', 'MENU', '/edu/lesson-hour', 'edu/LessonHourList', NULL, NULL, 'clock', 37, 'ENABLED', 1, 1),
  (130, NULL, 'resource', '资料库', 'MENU', '/resource', 'Layout', NULL, NULL, 'folder-open', 40, 'ENABLED', 1, 1),
  (131, 130, 'resource:material', '资料管理', 'MENU', '/resource/material', 'resource/MaterialList', NULL, NULL, 'files', 41, 'ENABLED', 1, 1),
  (132, 130, 'resource:audit', '资料审核', 'MENU', '/resource/audit', 'resource/MaterialAudit', NULL, NULL, 'badge-check', 42, 'ENABLED', 1, 1),
  (140, NULL, 'operation', '运营管理', 'MENU', '/operation', 'Layout', NULL, NULL, 'megaphone', 50, 'ENABLED', 1, 1),
  (141, 140, 'operation:activity', '活动管理', 'MENU', '/operation/activity', 'operation/ActivityList', NULL, NULL, 'party-popper', 51, 'ENABLED', 1, 1),
  (142, 140, 'operation:group', '拼班管理', 'MENU', '/operation/group', 'operation/GroupRequestList', NULL, NULL, 'users-round', 52, 'ENABLED', 1, 1),
  (150, NULL, 'finance', '财务中心', 'MENU', '/finance', 'Layout', NULL, NULL, 'wallet', 60, 'ENABLED', 1, 1),
  (151, 150, 'finance:order', '订单管理', 'MENU', '/finance/order', 'finance/OrderList', NULL, NULL, 'receipt', 61, 'ENABLED', 1, 1),
  (152, 150, 'finance:payment', '支付流水', 'MENU', '/finance/payment', 'finance/PaymentList', NULL, NULL, 'credit-card', 62, 'ENABLED', 1, 1),
  (160, NULL, 'report', '数据报表', 'MENU', '/report', 'report/ReportIndex', NULL, NULL, 'chart-column', 70, 'ENABLED', 1, 1),
  (201, NULL, 'teacher:homework:review', '老师点评作业', 'BUTTON', NULL, NULL, 'POST', '/api/teacher/homework/*/comment', NULL, 201, 'ENABLED', 1, 1),
  (202, NULL, 'teacher:attendance:check', '老师课堂考勤', 'BUTTON', NULL, NULL, 'POST', '/api/teacher/attendance/check', NULL, 202, 'ENABLED', 1, 1),
  (203, NULL, 'teacher:material:upload', '老师上传资料', 'BUTTON', NULL, NULL, 'POST', '/api/teacher/materials', NULL, 203, 'ENABLED', 1, 1),
  (301, NULL, 'student:homework:submit', '学生提交作业', 'BUTTON', NULL, NULL, 'POST', '/api/student/homework/*/submit', NULL, 301, 'ENABLED', 1, 1),
  (302, NULL, 'student:activity:join', '学生报名活动', 'BUTTON', NULL, NULL, 'POST', '/api/student/activities/*/join', NULL, 302, 'ENABLED', 1, 1),
  (303, NULL, 'student:group:join', '学生参与拼班', 'BUTTON', NULL, NULL, 'POST', '/api/student/group-requests/*/join', NULL, 303, 'ENABLED', 1, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT 1, id, 1, 1 FROM sys_permission WHERE deleted = 0
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT 2, id, 1, 1
FROM sys_permission
WHERE code IN (
  'dashboard', 'edu', 'edu:teacher', 'edu:student', 'edu:course', 'edu:class', 'edu:schedule',
  'edu:homework', 'edu:lesson-hour', 'resource', 'resource:material', 'resource:audit',
  'operation', 'operation:activity', 'operation:group', 'finance', 'finance:order',
  'finance:payment', 'report'
)
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT 3, id, 1, 1
FROM sys_permission
WHERE code IN (
  'dashboard', 'edu:class', 'edu:schedule', 'edu:homework', 'teacher:homework:review',
  'teacher:attendance:check', 'teacher:material:upload', 'resource:material'
)
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT 4, id, 1, 1
FROM sys_permission
WHERE code IN ('student:homework:submit', 'student:activity:join', 'student:group:join')
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, campus_id, created_by, updated_by) VALUES
  (1, 1, NULL, 1, 1),
  (2, 2, 1001, 1, 1),
  (3, 3, 1001, 2, 2),
  (4, 3, 1001, 2, 2),
  (5, 4, 1001, 2, 2),
  (6, 4, 1001, 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_campus (user_id, campus_id, relation_type, is_default, created_by, updated_by) VALUES
  (2, 1001, 'ADMIN', TRUE, 1, 1),
  (3, 1001, 'TEACHER', TRUE, 2, 2),
  (4, 1001, 'TEACHER', TRUE, 2, 2),
  (5, 1001, 'GUARDIAN', TRUE, 2, 2),
  (6, 1001, 'GUARDIAN', TRUE, 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO edu_teacher (
  id, campus_id, user_id, employee_no, name, gender, phone, title, specialties, intro, hire_date, status, created_by, updated_by
) VALUES
  (2001, 1001, 3, 'T-SUN-001', 'Amy老师', 'FEMALE', '13900000011', '主班老师', '["自然拼读","绘本阅读","剑桥少儿英语"]'::JSONB, '6年少儿英语教学经验，擅长启蒙和自然拼读。', DATE '2023-09-01', 'ENABLED', 2, 2),
  (2002, 1001, 4, 'T-SUN-002', 'Bob老师', 'MALE', '13900000012', '外教口语老师', '["口语表达","戏剧英语"]'::JSONB, '负责口语活动课与英语角。', DATE '2024-03-01', 'ENABLED', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_course (
  id, campus_id, course_code, course_system, name, level_name, target_age_min, target_age_max, grade_scope,
  total_hours, unit_price, package_price, description, status, created_by, updated_by
) VALUES
  (3001, 1001, 'PHONICS-L1', '自然拼读体系', '自然拼读L1', 'Level 1', 5, 8, '幼儿园大班-二年级', 48, 200, 9600, '字母音、短元音、CVC拼读和基础绘本阅读。', 'ENABLED', 2, 2),
  (3002, 1001, 'CAMBRIDGE-KET', '剑桥体系', 'KET基础冲刺', 'KET Foundation', 9, 12, '三年级-六年级', 60, 240, 14400, 'KET词汇、语法、听说读写综合训练。', 'ENABLED', 2, 2),
  (3003, 1001, 'READING-PIC', '绘本阅读体系', '英文绘本精读', 'Picture Book', 4, 9, '幼儿园-三年级', 36, 180, 6480, '分级绘本阅读、跟读和表达。', 'ENABLED', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_class (
  id, campus_id, course_id, class_no, name, head_teacher_id, classroom, class_wechat_qr_url,
  start_date, end_date, max_students, current_students, status, remark, created_by, updated_by
) VALUES
  (4001, 1001, 3001, 'SUN-PH-L1-A', '自然拼读L1-A班', 2001, '阳光校区 A教室', 'https://cdn.example.com/qr/sun-ph-l1-a.png', DATE '2026-06-01', DATE '2026-12-31', 8, 2, 'OPEN', '周六上午固定班', 2, 2),
  (4002, 1001, 3002, 'SUN-KET-A', 'KET基础A班', 2001, '阳光校区 B教室', 'https://cdn.example.com/qr/sun-ket-a.png', DATE '2026-06-05', DATE '2027-01-31', 10, 0, 'PREPARING', '拼班满员后开班', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_class_teacher (campus_id, class_id, teacher_id, role_type, created_by, updated_by) VALUES
  (1001, 4001, 2001, 'LEAD', 2, 2),
  (1001, 4001, 2002, 'ASSISTANT', 2, 2),
  (1001, 4002, 2001, 'LEAD', 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO edu_guardian (
  id, campus_id, user_id, name, phone, status, created_by, updated_by
) VALUES
  (6001, 1001, 5, '李女士', '13900000101', 'ENABLED', 2, 2),
  (6002, 1001, 6, '王先生', '13900000102', 'ENABLED', 2, 2),
  (6003, 1001, NULL, '陈女士', '13900000103', 'ENABLED', 2, 2),
  (6004, 1001, NULL, '赵先生', '13900000104', 'ENABLED', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_student (
  id, campus_id, student_no, name, nickname, gender, birthday, grade, school, english_level,
  learning_goal, status, enrolled_at, created_by, updated_by
) VALUES
  (7001, 1001, 'S-SUN-0001', '李小米', '小米', 'FEMALE', DATE '2018-05-12', '一年级', '阳光小学', '零基础', '完成自然拼读启蒙，建立朗读习惯。', 'ACTIVE', DATE '2026-05-10', 2, 2),
  (7002, 1001, 'S-SUN-0002', '王一诺', '一诺', 'MALE', DATE '2017-11-03', '二年级', '阳光小学', '自然拼读入门', '提升拼读和绘本阅读能力。', 'ACTIVE', DATE '2026-05-12', 2, 2),
  (7003, 1001, 'S-SUN-0003', '陈安安', '安安', 'FEMALE', DATE '2016-09-21', '三年级', '滨河小学', '校内同步', '准备KET基础班试听。', 'POTENTIAL', DATE '2026-05-18', 2, 2),
  (7004, 1001, 'S-SUN-0004', '赵小航', '小航', 'MALE', DATE '2016-01-08', '三年级', '阳光小学', '校内同步', '寻找KET同龄拼班。', 'POTENTIAL', DATE '2026-05-19', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_student_guardian (campus_id, student_id, guardian_id, relation, is_primary, created_by, updated_by) VALUES
  (1001, 7001, 6001, 'MOTHER', TRUE, 2, 2),
  (1001, 7002, 6002, 'FATHER', TRUE, 2, 2),
  (1001, 7003, 6003, 'MOTHER', TRUE, 2, 2),
  (1001, 7004, 6004, 'FATHER', TRUE, 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO edu_class_student (campus_id, class_id, student_id, join_date, status, created_by, updated_by) VALUES
  (1001, 4001, 7001, DATE '2026-06-01', 'ACTIVE', 2, 2),
  (1001, 4001, 7002, DATE '2026-06-01', 'ACTIVE', 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO edu_class_schedule (
  id, campus_id, class_id, course_id, teacher_id, lesson_no, lesson_date, start_time, end_time,
  topic, content, lesson_hours, status, classroom, created_by, updated_by
) VALUES
  (5001, 1001, 4001, 3001, 2001, 1, DATE '2026-06-06', TIME '09:00', TIME '10:30', '字母音与短元音a', '建立课堂规则，学习/a/音和CVC单词cat、map。', 2, 'FINISHED', '阳光校区 A教室', 2, 2),
  (5002, 1001, 4001, 3001, 2001, 2, DATE '2026-06-13', TIME '09:00', TIME '10:30', '短元音i与拼读练习', '复习/a/音，引入/i/音和简单绘本跟读。', 2, 'SCHEDULED', '阳光校区 A教室', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO res_file (
  id, campus_id, storage_type, bucket_name, object_key, url, file_name, content_type, file_size, uploader_id, biz_type, status, created_by, updated_by
) VALUES
  (9001, 1001, 'OSS', 'edu-demo', 'materials/phonics-l1-guide.pdf', 'https://cdn.example.com/materials/phonics-l1-guide.pdf', '自然拼读L1学习指南.pdf', 'application/pdf', 512000, 3, 'MATERIAL', 'AVAILABLE', 3, 3),
  (9002, 1001, 'OSS', 'edu-demo', 'materials/story-red-hen.mp4', 'https://cdn.example.com/materials/story-red-hen.mp4', 'The Little Red Hen 绘本课.mp4', 'video/mp4', 20480000, 3, 'MATERIAL', 'AVAILABLE', 3, 3),
  (9003, 1001, 'OSS', 'edu-demo', 'activities/english-corner-cover.jpg', 'https://cdn.example.com/activities/english-corner-cover.jpg', '英语角活动封面.jpg', 'image/jpeg', 256000, 2, 'ACTIVITY', 'AVAILABLE', 2, 2),
  (9004, 1001, 'OSS', 'edu-demo', 'qr/sun-ket-trial.png', 'https://cdn.example.com/qr/sun-ket-trial.png', 'KET试听群二维码.png', 'image/png', 88000, 2, 'GROUP_TRIAL', 'AVAILABLE', 2, 2),
  (9005, 1001, 'OSS', 'edu-demo', 'homework/phonics-audio-a.mp3', 'https://cdn.example.com/homework/phonics-audio-a.mp3', '短元音a跟读音频.mp3', 'audio/mpeg', 1800000, 3, 'HOMEWORK', 'AVAILABLE', 3, 3),
  (9006, 1001, 'OSS', 'edu-demo', 'submission/xiaomi-a-reading.m4a', 'https://cdn.example.com/submission/xiaomi-a-reading.m4a', '李小米朗读提交.m4a', 'audio/mp4', 1200000, 5, 'HOMEWORK_SUBMISSION', 'AVAILABLE', 5, 5),
  (9007, 1001, 'OSS', 'edu-demo', 'comment/amy-xiaomi-feedback.m4a', 'https://cdn.example.com/comment/amy-xiaomi-feedback.m4a', 'Amy点评语音.m4a', 'audio/mp4', 900000, 3, 'HOMEWORK_COMMENT', 'AVAILABLE', 3, 3),
  (9008, 1001, 'OSS', 'edu-demo', 'group/poster-ket-202606.png', 'https://cdn.example.com/group/poster-ket-202606.png', 'KET拼班邀请海报.png', 'image/png', 680000, 2, 'GROUP_POSTER', 'AVAILABLE', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO res_category (
  id, campus_id, parent_id, category_type, name, sort_order, status, created_by, updated_by
) VALUES
  (9101, 1001, NULL, 'MATERIAL', '绘本馆', 10, 'ENABLED', 2, 2),
  (9102, 1001, NULL, 'MATERIAL', '语法宝典', 20, 'ENABLED', 2, 2),
  (9103, 1001, NULL, 'MATERIAL', '视频课堂', 30, 'ENABLED', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO res_material (
  id, campus_id, category_id, title, description, resource_type, cover_file_id, file_id, owner_teacher_id,
  visibility, study_type, allow_download, audit_status, audit_by, audit_time, status, created_by, updated_by
) VALUES
  (9201, 1001, 9101, '自然拼读L1学习指南', '自然拼读L1课前预习和课后复习资料。', 'PDF', NULL, 9001, 2001, 'CLASS', 'REQUIRED', TRUE, 'APPROVED', 2, TIMESTAMPTZ '2026-05-25 10:00:00+08', 'PUBLISHED', 3, 2),
  (9202, 1001, 9103, 'The Little Red Hen 绘本课', '绘本故事视频，建议课后观看并跟读。', 'VIDEO', NULL, 9002, 2001, 'CAMPUS', 'OPTIONAL', FALSE, 'APPROVED', 2, TIMESTAMPTZ '2026-05-25 10:10:00+08', 'PUBLISHED', 3, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO res_material_class (campus_id, material_id, class_id, created_by, updated_by) VALUES
  (1001, 9201, 4001, 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO ops_activity (
  id, campus_id, title, description, cover_file_id, start_time, end_time, location, fee, quota, registered_count,
  status, published_at, created_by, updated_by
) VALUES
  (10001, 1001, '社区英语角：我的夏天', '通过小游戏和情景对话练习夏天主题表达。', 9003,
   TIMESTAMPTZ '2026-06-20 10:00:00+08', TIMESTAMPTZ '2026-06-20 11:30:00+08',
   '阳光花园中心广场', 99, 30, 1, 'PUBLISHED', TIMESTAMPTZ '2026-05-26 09:00:00+08', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO fin_order (
  id, campus_id, order_no, order_type, student_id, guardian_id, course_id, activity_id,
  total_amount, discount_amount, paid_amount, pay_status, pay_channel, transaction_no, pay_time, status, remark, created_by, updated_by
) VALUES
  (12001, 1001, 'CO202606010001', 'COURSE', 7001, 6001, 3001, NULL, 9600, 0, 9600, 'PAID', 'WECHAT', 'WX202606010001', TIMESTAMPTZ '2026-06-01 12:05:00+08', 'COMPLETED', '自然拼读L1 48课时', 2, 2),
  (12002, 1001, 'CO202606010002', 'COURSE', 7002, 6002, 3001, NULL, 9600, 0, 9600, 'PAID', 'WECHAT', 'WX202606010002', TIMESTAMPTZ '2026-06-01 12:15:00+08', 'COMPLETED', '自然拼读L1 48课时', 2, 2),
  (12003, 1001, 'AO202606020001', 'ACTIVITY', 7001, 6001, NULL, 10001, 99, 0, 99, 'PAID', 'WECHAT', 'WX202606020001', TIMESTAMPTZ '2026-06-02 18:20:00+08', 'COMPLETED', '英语角活动报名', 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO fin_payment_record (
  id, campus_id, order_id, payment_no, pay_channel, amount, transaction_no, status, paid_at, raw_response, created_by, updated_by
) VALUES
  (12101, 1001, 12001, 'PAY202606010001', 'WECHAT', 9600, 'WX202606010001', 'SUCCESS', TIMESTAMPTZ '2026-06-01 12:05:00+08', '{"mock":true}'::JSONB, 2, 2),
  (12102, 1001, 12002, 'PAY202606010002', 'WECHAT', 9600, 'WX202606010002', 'SUCCESS', TIMESTAMPTZ '2026-06-01 12:15:00+08', '{"mock":true}'::JSONB, 2, 2),
  (12103, 1001, 12003, 'PAY202606020001', 'WECHAT', 99, 'WX202606020001', 'SUCCESS', TIMESTAMPTZ '2026-06-02 18:20:00+08', '{"mock":true}'::JSONB, 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ops_activity_registration (
  id, campus_id, activity_id, student_id, guardian_id, order_id, registration_no, amount, status, registered_at, created_by, updated_by
) VALUES
  (10101, 1001, 10001, 7001, 6001, 12003, 'AR202606020001', 99, 'REGISTERED', TIMESTAMPTZ '2026-06-02 18:18:00+08', 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_lesson_hour_account (
  id, campus_id, student_id, course_id, purchased_hours, gift_hours, adjusted_hours, consumed_hours,
  refunded_hours, locked_hours, status, created_by, updated_by
) VALUES
  (13001, 1001, 7001, 3001, 48, 0, 0, 2, 0, 0, 'ACTIVE', 2, 2),
  (13002, 1001, 7002, 3001, 48, 0, 0, 0, 0, 0, 'ACTIVE', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_lesson_hour_record (
  id, campus_id, account_id, student_id, course_id, class_id, schedule_id, order_id, change_type,
  hours_delta, balance_after, operator_id, occurred_at, remark, created_by, updated_by
) VALUES
  (14001, 1001, 13001, 7001, 3001, NULL, NULL, 12001, 'PURCHASE', 48, 48, 2, TIMESTAMPTZ '2026-06-01 12:05:00+08', '课程购买入账', 2, 2),
  (14002, 1001, 13001, 7001, 3001, 4001, 5001, NULL, 'CONSUME', -2, 46, 3, TIMESTAMPTZ '2026-06-06 10:40:00+08', '第1次课课消', 3, 3),
  (14003, 1001, 13002, 7002, 3001, NULL, NULL, 12002, 'PURCHASE', 48, 48, 2, TIMESTAMPTZ '2026-06-01 12:15:00+08', '课程购买入账', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_attendance (
  id, campus_id, schedule_id, class_id, student_id, status, check_time, checked_by, consumed_hours, hour_record_id, remark, created_by, updated_by
) VALUES
  (14501, 1001, 5001, 4001, 7001, 'PRESENT', TIMESTAMPTZ '2026-06-06 09:05:00+08', 3, 2, 14002, '正常出勤', 3, 3),
  (14502, 1001, 5001, 4001, 7002, 'LEAVE', TIMESTAMPTZ '2026-06-06 09:10:00+08', 3, 0, NULL, '家长提前请假，不扣课时', 3, 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_homework (
  id, campus_id, teacher_id, title, content, target_type, deadline, checkin_enabled, checkin_cycle_days,
  status, published_at, created_by, updated_by
) VALUES
  (15001, 1001, 2001, '短元音a朗读打卡', '听附件音频，朗读cat/map/hat三组单词并提交语音。', 'CLASS',
   TIMESTAMPTZ '2026-06-12 21:00:00+08', TRUE, 7, 'PUBLISHED', TIMESTAMPTZ '2026-06-06 11:00:00+08', 3, 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_homework_target (campus_id, homework_id, class_id, student_id, created_by, updated_by) VALUES
  (1001, 15001, 4001, NULL, 3, 3)
ON CONFLICT DO NOTHING;

INSERT INTO edu_homework_attachment (campus_id, homework_id, file_id, sort_order, created_by, updated_by) VALUES
  (1001, 15001, 9005, 1, 3, 3)
ON CONFLICT DO NOTHING;

INSERT INTO edu_homework_submission (
  id, campus_id, homework_id, student_id, content, status, submitted_at, created_by, updated_by
) VALUES
  (15101, 1001, 15001, 7001, '已完成第一天朗读，请老师点评。', 'COMMENTED', TIMESTAMPTZ '2026-06-07 20:10:00+08', 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_homework_submission_file (campus_id, submission_id, file_id, media_type, sort_order, created_by, updated_by) VALUES
  (1001, 15101, 9006, 'AUDIO', 1, 5, 5)
ON CONFLICT DO NOTHING;

INSERT INTO edu_homework_comment (
  id, campus_id, submission_id, teacher_id, comment_text, voice_file_id, rating, commented_at, created_by, updated_by
) VALUES
  (15201, 1001, 15101, 2001, '发音很清楚，注意map中/m/音开头要更短促。', 9007, 5, TIMESTAMPTZ '2026-06-07 21:00:00+08', 3, 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO edu_homework_checkin (
  id, campus_id, homework_id, student_id, checkin_date, content, status, submitted_at, created_by, updated_by
) VALUES
  (15301, 1001, 15001, 7001, DATE '2026-06-07', '第一天朗读完成', 'SUBMITTED', TIMESTAMPTZ '2026-06-07 20:10:00+08', 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grp_rule (
  id, campus_id, min_members, trial_enabled, auto_fail_days, poster_template_file_id, status, created_by, updated_by
) VALUES
  (16001, 1001, 4, TRUE, 14, 9008, 'ENABLED', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grp_request (
  id, campus_id, request_no, initiator_student_id, initiator_guardian_id, child_age, grade, target_system,
  english_level, preferred_times, remark, required_members, current_members, status, share_code, poster_file_id,
  expires_at, created_by, updated_by
) VALUES
  (16101, 1001, 'GR202605270001', 7003, 6003, 9.5, '三年级', '剑桥KET体系', '校内同步',
   '[{"weekday":"SAT","period":"AM"},{"weekday":"SUN","period":"PM"}]'::JSONB,
   '希望同小区同龄孩子一起试听。', 4, 4, 'TRIAL_ARRANGED', 'KET202606', 9008,
   TIMESTAMPTZ '2026-06-10 23:59:59+08', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grp_member (
  id, campus_id, request_id, student_id, guardian_id, nickname, avatar_url, phone, join_status, joined_at, created_by, updated_by
) VALUES
  (16201, 1001, 16101, 7003, 6003, '安安', NULL, '13900000103', 'JOINED', TIMESTAMPTZ '2026-05-27 09:20:00+08', 2, 2),
  (16202, 1001, 16101, 7004, 6004, '小航', NULL, '13900000104', 'JOINED', TIMESTAMPTZ '2026-05-27 10:00:00+08', 2, 2),
  (16203, 1001, 16101, NULL, NULL, 'Mia', NULL, '13900000105', 'JOINED', TIMESTAMPTZ '2026-05-27 14:12:00+08', 2, 2),
  (16204, 1001, 16101, NULL, NULL, 'Leo', NULL, '13900000106', 'JOINED', TIMESTAMPTZ '2026-05-27 15:35:00+08', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grp_trial (
  id, campus_id, request_id, trial_time, location, teacher_id, class_id, wechat_qr_file_id,
  status, arranged_by, arranged_at, remark, created_by, updated_by
) VALUES
  (16301, 1001, 16101, TIMESTAMPTZ '2026-06-07 15:00:00+08', '阳光校区 B教室', 2001, 4002, 9004,
   'ARRANGED', 2, TIMESTAMPTZ '2026-05-27 16:00:00+08', '试听后根据水平拆分正式班。', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grp_trial_feedback (
  id, campus_id, trial_id, member_id, student_id, feedback, result, next_action, operator_id, created_by, updated_by
) VALUES
  (16401, 1001, 16301, 16201, 7003, '词汇基础较好，口语输出偏谨慎，适合KET基础A班。', 'FOLLOW_UP', '试听后电话沟通报名意向', 2, 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_notification (
  id, campus_id, receiver_user_id, receiver_student_id, biz_type, biz_id, title, content, status, created_by, updated_by
) VALUES
  (17001, 1001, 5, 7001, 'HOMEWORK_COMMENT', 15201, '作业已点评', 'Amy老师已点评李小米的短元音a朗读作业。', 'UNREAD', 3, 3),
  (17002, 1001, 2, NULL, 'GROUP_TRIAL', 16301, '拼班试听已安排', 'KET拼班已达到4人并安排试听。', 'UNREAD', 2, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_operation_log (
  id, campus_id, operator_id, module_name, operation_name, biz_type, biz_id, request_method, request_uri,
  ip, user_agent, content, result_status, created_by, updated_by
) VALUES
  (18001, 1001, 3, '课时管理', '确认课消', 'LESSON_HOUR_RECORD', 14002, 'POST', '/api/teacher/lesson-hours/consume',
   '127.0.0.1', 'seed-script', '{"scheduleId":5001,"studentId":7001,"hours":2}'::JSONB, 'SUCCESS', 3, 3),
  (18002, 1001, 2, '拼班管理', '安排试听', 'GROUP_TRIAL', 16301, 'POST', '/api/admin/group-requests/16101/trial',
   '127.0.0.1', 'seed-script', '{"trialTime":"2026-06-07T15:00:00+08:00"}'::JSONB, 'SUCCESS', 2, 2)
ON CONFLICT (id) DO NOTHING;

DO $$
DECLARE
  table_name TEXT;
  sequence_name TEXT;
  max_id BIGINT;
BEGIN
  FOREACH table_name IN ARRAY ARRAY[
    'sys_campus',
    'sys_user',
    'sys_role',
    'sys_permission',
    'sys_role_permission',
    'sys_user_role',
    'sys_user_campus',
    'edu_teacher',
    'edu_course',
    'edu_class',
    'edu_class_teacher',
    'edu_guardian',
    'edu_student',
    'edu_student_guardian',
    'edu_class_student',
    'edu_class_schedule',
    'res_file',
    'res_category',
    'res_material',
    'res_material_class',
    'ops_activity',
    'fin_order',
    'fin_payment_record',
    'ops_activity_registration',
    'edu_lesson_hour_account',
    'edu_lesson_hour_record',
    'edu_attendance',
    'edu_homework',
    'edu_homework_target',
    'edu_homework_attachment',
    'edu_homework_submission',
    'edu_homework_submission_file',
    'edu_homework_comment',
    'edu_homework_checkin',
    'grp_rule',
    'grp_request',
    'grp_member',
    'grp_trial',
    'grp_trial_feedback',
    'sys_notification',
    'sys_operation_log'
  ]
  LOOP
    SELECT pg_get_serial_sequence(table_name, 'id') INTO sequence_name;
    IF sequence_name IS NOT NULL THEN
      EXECUTE FORMAT('SELECT MAX(id) FROM %I', table_name) INTO max_id;
      IF max_id IS NULL OR max_id = 0 THEN
        EXECUTE FORMAT('SELECT setval(%L, 1, false)', sequence_name);
      ELSE
        EXECUTE FORMAT('SELECT setval(%L, %s, true)', sequence_name, max_id);
      END IF;
    END IF;
  END LOOP;
END;
$$;
