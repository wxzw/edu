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
