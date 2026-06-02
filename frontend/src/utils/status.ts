export const statusText: Record<string, string> = {
  ENABLED: '启用',
  DISABLED: '停用',
  ACTIVE: '在读',
  INACTIVE: '停用',
  OPEN: '开班中',
  PREPARING: '筹备中',
  CLOSED: '已结班',
};

export const statusType = (status?: string): 'success' | 'warning' | 'info' | 'danger' => {
  if (status === 'ENABLED' || status === 'ACTIVE' || status === 'OPEN') return 'success';
  if (status === 'PREPARING') return 'warning';
  if (status === 'DISABLED' || status === 'CLOSED') return 'danger';
  return 'info';
};

export const accountTypeText: Record<string, string> = {
  SUPER_ADMIN: '超级管理员',
  CAMPUS_ADMIN: '校区管理员',
  TEACHER: '老师',
  GUARDIAN: '家长',
};

export const dataScopeText: Record<string, string> = {
  ALL: '全部校区',
  CAMPUS: '当前校区',
  SELF: '本人数据',
};
