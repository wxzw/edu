# 教师资料上传功能 — 实施计划

## 一、需求概述

老师通过微信小程序上传教学资料（图片/文档/视频/音频/PDF），管理后台审核后发布，学生登录小程序可查看和下载。

## 二、现有基础设施盘点

| 层级 | 已有 | 状态 |
|------|------|------|
| **DB 表** | `res_file`、`res_category`、`res_material`、`res_material_class` | ✅ 已建表，字段完整（含 `audit_status`、`visibility`、`allow_download`） |
| **后端 — 管理端** | `AdminP1Controller` / `AdminP1Service` 资料 CRUD + 分类管理 + 上下架 | ✅ 已实现 |
| **后端 — 学生端** | `StudentP1Controller` / `StudentP1Service` 资料列表/详情/预览/下载 | ✅ 已实现 |
| **后端 — 文件上传** | `LocalFileStorageService.store()` → `StoredFile(objectKey, fileName, contentType, fileSize)` | ✅ 已实现 |
| **后端 — 教师端** | `TeacherController` / `TeacherApiService` — 无资料相关接口 | ❌ 缺失 |
| **前端 — 学生端** | `student/material/detail.vue` + `api/student.ts` (getMaterials/getMaterialDetail) | ✅ 已实现 |
| **前端 — 教师端** | `material/upload.vue`（空壳）、`material/list.vue`（空壳）、`api/teacher.ts`（无资料 API） | ❌ 需实现 |
| **前端 — 管理端** | 管理后台资料审核（Web 管理系统） | ⚠️ 需增加审核端点 |

## 三、架构设计

### 3.1 业务流程

```
教师小程序                          管理后台                         学生小程序
   │                                  │                                │
   ├─ 1. 选择文件 (图片/文档/视频/音频) │                                │
   ├─ 2. 填写标题/描述/分类/可见范围    │                                │
   ├─ 3. 调用上传接口 ──────────────►  │                                │
   │     POST /api/teacher/files       │                                │
   ├─ 4. 创建资料 ──────────────────►  │                                │
   │     POST /api/teacher/materials   │                                │
   │     (audit_status=PENDING)       │                                │
   │                                  ├─ 5. 查看待审核列表              │
   │                                  ├─ 6. 审核通过/驳回               │
   │                                  │     PATCH /api/admin/materials/{id}/audit
   │                                  │     → audit_status=APPROVED    │
   │                                  │     → status=PUBLISHED          │
   │                                  │                                ├─ 7. 查看已发布资料
   │                                  │                                ├─ 8. 预览/下载
   │                                  │                                │     GET /api/student/materials/{id}/preview
   │                                  │                                │     GET /api/student/materials/{id}/download
   ├─ 9. 查看自己上传的资料状态        │                                │
   │     GET /api/teacher/materials   │                                │
   └─ 10. 删除草稿                    │                                │
        DELETE /api/teacher/materials/{id}
```

### 3.2 权限与可见性

- **audit_status 流转**: `PENDING` → `APPROVED` / `REJECTED`
- **status 流转**: 审核通过后自动变为 `PUBLISHED`，管理后台可手动 `DRAFT`/`PUBLISHED`/`ARCHIVED`
- **visibility**:
  - `CAMPUS`: 校区所有学生可见
  - `CLASS`: 仅指定班级可见（通过 `res_material_class` 表）
  - `SELF`: 仅上传者自己可见（学生端不可见）

## 四、实施任务

### 任务 1：后端 — 教师端文件上传接口

**文件**: `TeacherController.java` + `TeacherApiService.java`

新增接口:
```
POST /api/teacher/files
Content-Type: multipart/form-data
参数: file (MultipartFile), bizType (可选，默认 MATERIAL)
返回: { fileId, fileName, contentType, fileSize, objectKey }
```

**实现要点**:
- 复用 `LocalFileStorageService.store(file, campusId)` 保存文件
- 在 `res_file` 表插入记录（storage_type=LOCAL, biz_type=MATERIAL）
- 返回文件元信息供前端后续创建资料时引用

**涉及文件**:
- `TeacherController.java` — 新增 `uploadFile` 端点
- `TeacherApiService.java` — 新增 `uploadFile` 方法
- `TeacherMiniappMapper.java` / `TeacherMiniappMapper.xml` — 新增 `insertFile` SQL

---

### 任务 2：后端 — 教师端资料 CRUD 接口

**文件**: `TeacherController.java` + `TeacherApiService.java` + DTO

新增接口:
```
GET    /api/teacher/materials              — 我的资料列表（支持 auditStatus 筛选）
POST   /api/teacher/materials              — 创建资料（audit_status=PENDING）
GET    /api/teacher/materials/{id}         — 资料详情
DELETE /api/teacher/materials/{id}         — 删除草稿/已驳回资料
```

**请求体** (`CreateMaterialRequest`):
```json
{
  "title": "自然拼读L1复习讲义",
  "description": "适用A班，课后复习使用",
  "categoryId": 1,
  "resourceType": "PDF",
  "visibility": "CAMPUS",
  "studyType": "REQUIRED",
  "allowDownload": true,
  "fileId": 123,
  "coverFileId": null,
  "classIds": [1, 2]
}
```

**响应体** (`MaterialItem`):
```json
{
  "id": 1,
  "title": "...",
  "description": "...",
  "categoryId": 1,
  "categoryName": "英语",
  "resourceType": "PDF",
  "visibility": "CAMPUS",
  "studyType": "REQUIRED",
  "allowDownload": true,
  "auditStatus": "PENDING",
  "status": "DRAFT",
  "fileId": 123,
  "fileName": "review.pdf",
  "fileSize": 1024000,
  "contentType": "application/pdf",
  "coverUrl": null,
  "createdAt": "2026-06-07T10:00:00Z",
  "rejectedReason": null
}
```

**涉及文件**:
- `TeacherController.java` — 新增 4 个端点
- `TeacherApiService.java` — 新增 4 个方法
- `TeacherRequests.java` — 新增 `CreateMaterialRequest` 内部类
- `TeacherResponses.java` — 新增 `MaterialItem` 内部类
- `TeacherMiniappMapper.java` / `TeacherMiniappMapper.xml` — 新增 SQL

---

### 任务 3：后端 — 管理端资料审核接口

**文件**: `AdminP1Controller.java` + `AdminP1Service.java`

新增接口:
```
GET   /api/admin/materials?auditStatus=PENDING  — 已有，增加 auditStatus 筛选
PATCH /api/admin/materials/{id}/audit           — 审核通过/驳回
```

**请求体** (`AuditMaterialRequest`):
```json
{
  "auditStatus": "APPROVED",
  "rejectedReason": ""
}
```

**审核逻辑**:
- `APPROVED`: 设置 `audit_status=APPROVED`, `status=PUBLISHED`, `audit_by=当前用户`, `audit_time=NOW()`
- `REJECTED`: 设置 `audit_status=REJECTED`, `audit_by=当前用户`, `audit_time=NOW()`, 保留 `rejected_reason`
- 仅 `PENDING` 状态的资料可审核

**涉及文件**:
- `AdminP1Controller.java` — 新增审核端点
- `AdminP1Service.java` — 新增审核方法
- `AdminP1Requests.java` — 新增 `AuditMaterialRequest`
- `AdminP1Mapper` — 新增审核 SQL

---

### 任务 4：后端 — 教师端资料分类查询

**文件**: `TeacherController.java` + `TeacherApiService.java`

新增接口:
```
GET /api/teacher/material-categories — 获取本校区资料分类列表
```

**实现要点**:
- 查询 `res_category` 表，条件: `campus_id=? AND category_type='MATERIAL' AND status='ENABLED' AND deleted=0`
- 按 `sort_order` 排序

---

### 任务 5：前端 — 教师端 API 层

**文件**: `miniapp/src/api/teacher.ts`

新增函数:
```typescript
// 文件上传
export function uploadTeacherFile(filePath: string, fileName: string): Promise<TeacherFileResult>

// 资料分类
export function getTeacherMaterialCategories(): Promise<MaterialCategory[]>

// 资料 CRUD
export function getTeacherMaterials(auditStatus?: string): Promise<TeacherMaterialItem[]>
export function createTeacherMaterial(data: CreateMaterialRequest): Promise<TeacherMaterialItem>
export function deleteTeacherMaterial(id: number): Promise<void>
```

**文件**: `miniapp/src/types/api.ts`

新增类型:
```typescript
export interface TeacherFileResult {
  fileId: number;
  fileName: string;
  contentType: string;
  fileSize: number;
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
```

---

### 任务 6：前端 — 教师端资料上传页

**文件**: `miniapp/src/pages/teacher/material/upload.vue`

改造现有空壳页面，新增功能:
1. **文件选择区**: 点击选择文件，支持图片/文档/视频/音频/PDF
   - 图片: `uni.chooseImage` → 预览缩略图
   - 视频: `uni.chooseVideo` → 显示视频信息
   - 文档/PDF/音频: `uni.chooseFile`（或 `uni.chooseMessageFile`）→ 显示文件名和大小
2. **分类选择**: 从 API 加载分类列表，picker 选择
3. **表单字段**: 标题、描述、资源类型、可见范围、学习类型、是否允许下载
4. **班级多选** (visibility=CLASS时): 加载教师班级列表，checkbox 多选
5. **提交流程**:
   - 先调用 `uploadTeacherFile` 上传文件 → 获取 fileId
   - 再调用 `createTeacherMaterial` 创建资料记录

**UI 设计**:
- 暖色调设计系统（beige #f4efe6 背景，深绿 #1f5a44 主色，金色 #f0b84d 强调）
- 文件选择区域使用虚线边框 + 图标，点击触发选择
- 已选文件显示文件名/大小/类型图标，支持删除重新选择
- 提交按钮固定底部，loading 状态防重复提交

---

### 任务 7：前端 — 教师端资料列表页

**文件**: `miniapp/src/pages/teacher/material/list.vue`

改造现有空壳页面:
1. **Tab 筛选**: 全部 / 待审核(PENDING) / 已通过(APPROVED) / 已驳回(REJECTED)
2. **资料卡片**: 显示标题、资源类型标签、分类名、审核状态标签、创建时间
3. **状态标签颜色**:
   - 待审核: 金色 #f0b84d
   - 已通过: 绿色 #1f5a44
   - 已驳回: 红色 #c0392b
4. **操作**: 删除（仅草稿/已驳回可删除）
5. **空状态**: 引导教师上传资料
6. **下拉刷新 + 上拉加载更多**

---

### 任务 8：前端 — 学生端资料列表页（新增）

**文件**: `miniapp/src/pages/student/material/list.vue` (新建)

功能:
1. **分类筛选**: 顶部横向滚动分类标签
2. **资料列表**: 卡片式展示已发布资料
   - 封面图/类型图标 + 标题 + 分类名 + 学习类型标签
3. **点击跳转**: 进入 `student/material/detail` 查看详情
4. **空状态**: 暂无可用资料

**路由注册**: `pages.json` 新增 `pages/student/material/list`

---

### 任务 9：前端 — 学生端首页入口

**文件**: `miniapp/src/pages/student/home.vue`

在学生首页增加"教学资料"入口卡片，点击跳转到 `student/material/list`。

---

## 五、文件变更清单

### 后端新增/修改

| 文件 | 操作 | 说明 |
|------|------|------|
| `TeacherController.java` | 修改 | 新增 6 个端点 (uploadFile, materialCategories, materials CRUD) |
| `TeacherApiService.java` | 修改 | 新增对应 Service 方法 |
| `TeacherRequests.java` | 修改 | 新增 `CreateMaterialRequest` |
| `TeacherResponses.java` | 修改 | 新增 `MaterialItem`、`FileUploadResult` |
| `TeacherMiniappMapper.java` | 修改 | 新增 Mapper 方法声明 |
| `TeacherMiniappMapper.xml` | 修改 | 新增 SQL (insertFile, insertMaterial, selectMaterials, deleteMaterial, selectCategories) |
| `AdminP1Controller.java` | 修改 | 新增审核端点 |
| `AdminP1Service.java` | 修改 | 新增审核方法 |
| `AdminP1Requests.java` | 修改 | 新增 `AuditMaterialRequest` |
| `AdminP1Mapper.java` / `.xml` | 修改 | 新增审核 SQL |

### 前端新增/修改

| 文件 | 操作 | 说明 |
|------|------|------|
| `miniapp/src/api/teacher.ts` | 修改 | 新增资料相关 API 函数 |
| `miniapp/src/types/api.ts` | 修改 | 新增资料相关 TypeScript 类型 |
| `miniapp/src/pages/teacher/material/upload.vue` | 重写 | 实现完整上传功能 |
| `miniapp/src/pages/teacher/material/list.vue` | 重写 | 实现资料列表 + 审核状态 |
| `miniapp/src/pages/student/material/list.vue` | 新建 | 学生端资料列表页 |
| `miniapp/src/pages/student/home.vue` | 修改 | 增加资料入口 |
| `miniapp/src/pages.json` | 修改 | 注册学生资料列表页路由 |

## 六、实施顺序

1. **后端先行**: 任务 1 → 任务 4 → 任务 2 → 任务 3
2. **前端跟进**: 任务 5 → 任务 6 → 任务 7 → 任务 8 → 任务 9
3. **联调测试**: 使用 Swagger UI 测试后端接口，微信开发者工具测试小程序

## 七、技术要点

### 文件上传（微信小程序）
- 微信小程序不支持 `uni.chooseFile`，需使用:
  - 图片: `uni.chooseImage({ count: 1, success })`
  - 视频: `uni.chooseVideo({ success })`
  - 文档/PDF/音频: `wx.chooseMessageFile({ count: 1, type: 'file', success })` 或 `uni.chooseFile`（H5）
- 上传使用 `uni.uploadFile({ url, filePath, name: 'file', header: authHeaders })`

### 文件大小限制
- 微信小程序 `uploadFile` 单文件上限 100MB（实际建议 50MB）
- 后端 `spring.servlet.multipart.max-file-size=50MB`

### 安全
- 教师上传文件自动设置 `audit_status=PENDING`，学生端查询仅返回 `audit_status=APPROVED AND status=PUBLISHED` 的资料
- 文件下载需鉴权（通过 auth header）
