ALTER TABLE edu_student
  ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES sys_user (id);

COMMENT ON COLUMN edu_student.user_id IS 'Login user id for direct student miniapp login';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_wx_open_id_alive
  ON sys_user (wx_open_id) WHERE wx_open_id IS NOT NULL AND deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_wx_union_id_alive
  ON sys_user (wx_union_id) WHERE wx_union_id IS NOT NULL AND deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_edu_student_user_alive
  ON edu_student (campus_id, user_id) WHERE user_id IS NOT NULL AND deleted = 0;

INSERT INTO sys_role (
  campus_id, code, name, scope_type, data_scope, status, remark, created_by, updated_by
)
SELECT NULL, 'STUDENT', '学生', 'SYSTEM', 'SELF', 'ENABLED', '学生本人小程序权限', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role WHERE code = 'STUDENT' AND deleted = 0
);

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT r.id, p.id, 1, 1
FROM sys_role r
JOIN sys_permission p ON p.code IN (
  'student:homework:submit',
  'student:activity:join',
  'student:group:join'
)
WHERE r.code = 'STUDENT'
  AND r.deleted = 0
  AND p.deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_permission existing
    WHERE existing.role_id = r.id
      AND existing.permission_id = p.id
      AND existing.deleted = 0
  );

INSERT INTO sys_user (
  username, password_hash, real_name, phone, account_type, status, created_by, updated_by
)
SELECT 'student_lucy', '{bcrypt}' || crypt('123456', gen_salt('bf', 10)), 'Lucy', '13900000201',
       'STUDENT', 'ENABLED', 2, 2
WHERE NOT EXISTS (
  SELECT 1 FROM sys_user WHERE username = 'student_lucy' AND deleted = 0
);

INSERT INTO sys_user_role (user_id, role_id, campus_id, created_by, updated_by)
SELECT user_account.id, r.id, 1001, 2, 2
FROM sys_user user_account
JOIN sys_role r ON r.code = 'STUDENT' AND r.deleted = 0
WHERE user_account.username = 'student_lucy'
  AND user_account.deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM sys_user_role existing
    WHERE existing.user_id = user_account.id
      AND existing.role_id = r.id
      AND existing.campus_id = 1001
      AND existing.deleted = 0
  );

INSERT INTO sys_user_campus (user_id, campus_id, relation_type, is_default, created_by, updated_by)
SELECT user_account.id, 1001, 'STUDENT', TRUE, 2, 2
FROM sys_user user_account
WHERE user_account.username = 'student_lucy'
  AND user_account.deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM sys_user_campus existing
    WHERE existing.user_id = user_account.id
      AND existing.campus_id = 1001
      AND existing.relation_type = 'STUDENT'
      AND existing.deleted = 0
  );

UPDATE edu_student
SET user_id = (
  SELECT id FROM sys_user WHERE username = 'student_lucy' AND deleted = 0 LIMIT 1
)
WHERE campus_id = 1001
  AND id = 7001
  AND user_id IS NULL
  AND deleted = 0;
