INSERT INTO sys_permission (
  id, parent_id, code, name, permission_type, route_path, component, api_method, api_path, icon, sort_order, status, created_by, updated_by
)
SELECT 114, 110, 'system:notification', '通知管理', 'MENU', '/system/notification', 'system/NotificationList',
       NULL, NULL, 'bell', 24, 'ENABLED', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_permission WHERE code = 'system:notification' AND deleted = 0
);

INSERT INTO sys_role_permission (role_id, permission_id, created_by, updated_by)
SELECT r.id, p.id, 1, 1
FROM sys_role r
JOIN sys_permission p ON p.code = 'system:notification' AND p.deleted = 0
WHERE r.code IN ('SUPER_ADMIN', 'CAMPUS_ADMIN')
  AND r.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permission existing
      WHERE existing.role_id = r.id
        AND existing.permission_id = p.id
        AND existing.deleted = 0
  );
