export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  traceId: string;
  timestamp: string;
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  pageNo: number;
  pageSize: number;
}

export interface PageQuery {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  status?: string;
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
  accountType: string;
  roles: string[];
  permissions: string[];
  campusList: CampusOption[];
  defaultCampusId?: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface StatusUpdateRequest {
  status: string;
}
