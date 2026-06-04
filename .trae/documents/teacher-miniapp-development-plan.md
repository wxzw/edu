# 老师端小程序分步开发计划

> **目标：** 基于现有 Spring Boot + PostgreSQL 后端架构，实现老师端小程序完整功能，复用现有数据库和认证体系。
> **计划日期：** 2026-06-03
> **参考文档：** `STUDENT_MINIAPP_PLAN.md`（学生端规划文件命名参考）

---

## 一、文件整理

### 1.1 设计文件移动/重命名

| 当前路径 | 目标路径 | 操作 |
|---------|---------|------|
| `miniapp/.trae/documents/2026-06-03-teacher-miniapp-prd-plan.md` | `TEACHER_MINIAPP_PRD.md` | 移动到根目录，参考学生端命名 |
| `miniapp/.trae/documents/2026-06-03-teacher-miniapp-implementation-plan.md` | `TEACHER_MINIAPP_PLAN.md` | 移动到根目录，重写为 Spring Boot 版本（原 NestJS 版本废弃） |
| `miniapp/.trae/documents/miniapp-style-optimization-plan.md` | 保留原处 | 样式优化计划（通用，不移动） |

---

## 二、后端模块划分

### 2.1 架构决策

**新建 `com.community.edu.teacher` 包**，与学生端 `com.community.edu.student` 平行。

理由：
- 老师端和学生端是独立的业务领域，数据查询维度完全不同（学生端以 studentId 过滤，老师端以 teacherId 过滤）
- 接口路径独立：`/api/teacher/*` vs `/api/student/*`
- 避免在一个 Controller/Service 中混合两种身份的逻辑
- 遵循已有包命名惯例（`student`, `admin`, `auth`, `miniapp` 都是平级）

### 2.2 建议后端文件结构

```
backend/src/main/java/com/community/edu/
├── teacher/
│   ├── TeacherController.java          # /api/teacher/* 主控制器
│   ├── TeacherP1Controller.java        # /api/teacher/* P1 增强控制器
│   ├── TeacherApiService.java          # 核心业务逻辑
│   ├── TeacherP1Service.java           # P1 增强业务逻辑
│   ├── TeacherScopeService.java        # 老师身份作用域解析
│   └── dto/
│       ├── TeacherResponses.java       # 响应 DTO（内部静态类）
│       ├── TeacherRequests.java        # 请求 DTO
│       ├── TeacherMiniappRows.java     # Mapper 行对象
│       └── TeacherP1Responses.java     # P1 响应 DTO
├── mapper/
│   ├── TeacherMiniappMapper.java       # 老师端注解 SQL Mapper
│   └── TeacherP1Mapper.java            # P1 Mapper
└── student/                            # 已有，保持不变
```

### 2.3 老师端 Scope 设计

参考 `StudentScopeService`，新建 `TeacherScopeService`：

```java
@Service
@RequiredArgsConstructor
public class TeacherScopeService {
    public static final String TEACHER_ID_HEADER = "X-Teacher-Id";
    
    public TeacherContext resolve() {
        CurrentUser currentUser = CurrentUserHolder.getRequired();
        // 验证当前身份为 TEACHER
        // 返回 campusId + teacherId
    }
    
    public record TeacherContext(
        CurrentUser currentUser, 
        MiniappIdentityResponse identity,
        Long campusId, 
        Long teacherId
    ) {}
}
```

---

## 三、分步开发计划

### Phase 1：基础设施

#### Task 1：老师端 Scope 与 Controller 骨架

**目标：** 建立老师端后端基础设施，包括 Scope 解析、Controller 骨架、DTO 结构。

**涉及文件：**
- 新建：`backend/src/main/java/com/community/edu/teacher/TeacherScopeService.java`
- 新建：`backend/src/main/java/com/community/edu/teacher/TeacherController.java`
- 新建：`backend/src/main/java/com/community/edu/teacher/dto/TeacherResponses.java`
- 新建：`backend/src/main/java/com/community/edu/teacher/dto/TeacherRequests.java`
- 新建：`backend/src/main/java/com/community/edu/teacher/dto/TeacherMiniappRows.java`
- 新建：`backend/src/main/java/com/community/edu/mapper/TeacherMiniappMapper.java`
- 修改：`backend/src/main/java/com/community/edu/config/SecurityConfig.java`（添加 `/api/teacher/**` 白名单或权限规则）

**关键设计：**
- `TeacherScopeService.resolve()` 从 `X-Identity-Type` + `X-Identity-Id` 头部解析老师身份
- 验证 `identityType == "TEACHER"`，否则抛出 `BizException(FORBIDDEN)`
- 返回 `campusId` + `teacherId`，后续所有查询以此过滤

**验收标准：**
- [ ] `TeacherScopeService` 能正确解析老师身份
- [ ] 非老师身份访问 `/api/teacher/*` 返回 403
- [ ] Controller 骨架能响应测试请求

---

### Phase 2：P0 核心功能

#### Task 2：老师端首页仪表盘

**目标：** 实现老师登录后进入的首页，展示今日课程、待办事项、快捷统计。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/dashboard` 接口
  - `TeacherApiService`：实现 dashboard 聚合逻辑
  - `TeacherMiniappMapper`：添加今日课程、待办统计 SQL
- 前端：
  - 重写：`miniapp/src/pages/teacher/home.vue`
  - 新建：`miniapp/src/api/teacher.ts`
  - 修改：`miniapp/src/types/api.ts`（追加老师端类型）

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/dashboard` | GET | 老师首页聚合数据 |

**Dashboard 返回字段：**
```java
public static class Dashboard {
    private TeacherProfile profile;           // 老师姓名、头像、校区
    private List<TodaySchedule> todaySchedules; // 今日课程列表
    private TodoStats todoStats;              // 待考勤数、待点评数
    private QuickStats quickStats;            // 班级数、学生总数
}
```

**验收标准：**
- [ ] 老师身份登录后首页展示今日课程卡片（时间、班级、主题、教室）
- [ ] 待考勤、待点评数字准确统计
- [ ] 数据按当前老师 ID 和校区 ID 隔离
- [ ] 下拉刷新更新数据

---

#### Task 3：班级与学生管理

**目标：** 我的班级列表 → 班级学生列表 → 学生档案详情。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/classes`, `/classes/{id}`, `/classes/{id}/students`, `/students/{id}`
  - `TeacherApiService`：实现班级/学生查询逻辑
  - `TeacherMiniappMapper`：添加班级/学生 SQL
- 前端：
  - 新建：`miniapp/src/pages/teacher/class/list.vue`
  - 新建：`miniapp/src/pages/teacher/class/detail.vue`
  - 新建：`miniapp/src/pages/teacher/student/profile.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/classes` | GET | 我的班级列表 |
| `/api/teacher/classes/{id}` | GET | 班级详情 |
| `/api/teacher/classes/{id}/students` | GET | 班级学生列表 |
| `/api/teacher/students/{id}` | GET | 学生档案详情 |
| `/api/teacher/students/{id}/lesson-hours` | GET | 学生课时账户 |
| `/api/teacher/students/{id}/lesson-records` | GET | 学生课消记录 |

**学生档案展示内容：**
- 基本信息：姓名、昵称、头像、年级、学校
- 课时概览：剩余课时、课程维度拆分
- 出勤统计：本学期出勤率
- 作业完成率：本学期作业提交/点评统计
- 所在班级列表

**验收标准：**
- [ ] 老师只能查看自己任课或担任班主任的班级
- [ ] 学生列表展示姓名、剩余课时、最近考勤状态
- [ ] 学生档案课时数据与数据库一致
- [ ] 家长联系方式脱敏展示

---

#### Task 4：作业管理（发布 + 列表 + 批改）

**目标：** 老师发布作业、查看作业列表、批改学生提交（文字+语音点评）。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/homeworks`, `/homeworks/{id}`, `/homeworks/{id}/submissions`, `/homeworks/{id}/submissions/{sid}/comment`
  - `TeacherApiService`：实现作业发布、批改逻辑
  - `TeacherMiniappMapper`：添加作业相关 SQL
- 前端：
  - 新建：`miniapp/src/pages/teacher/homework/list.vue`
  - 新建：`miniapp/src/pages/teacher/homework/create.vue`
  - 新建：`miniapp/src/pages/teacher/homework/detail.vue`
  - 新建：`miniapp/src/pages/teacher/homework/comment.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/homeworks` | GET | 我发布的作业列表 |
| `/api/teacher/homeworks` | POST | 发布作业 |
| `/api/teacher/homeworks/{id}` | GET | 作业详情（含提交统计） |
| `/api/teacher/homeworks/{id}/submissions` | GET | 作业提交列表 |
| `/api/teacher/homeworks/{id}/submissions/{sid}/comment` | POST | 批改作业 |
| `/api/teacher/homeworks/{id}/publish` | POST | 发布草稿作业 |

**作业发布字段：**
- 标题、内容、附件（fileIds）
- 目标类型：CLASS / STUDENT
- 目标班级/学生 IDs
- 截止时间
- 是否启用打卡、打卡周期天数

**批改字段：**
- 点评文字
- 语音文件 ID（录音上传）
- 评分（1-5 星）
- 状态：COMMENTED / RESUBMIT_REQUIRED

**验收标准：**
- [ ] 老师可选择自己任课的班级发布作业
- [ ] 作业发布后目标学生可在学生端看到
- [ ] 学生提交后老师端待点评数 +1
- [ ] 支持文字点评和语音点评（语音最长 60 秒）
- [ ] 批改后学生端可查看点评内容

---

#### Task 5：考勤管理

**目标：** 按课程一键全勤 + 个别修改考勤状态。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/schedules/today`, `/schedules/{id}/attendance`
  - `TeacherApiService`：实现考勤逻辑
  - `TeacherMiniappMapper`：添加考勤 SQL
- 前端：
  - 新建：`miniapp/src/pages/teacher/attendance/list.vue`
  - 新建：`miniapp/src/pages/teacher/attendance/detail.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/schedules/today` | GET | 今日课程列表（考勤入口） |
| `/api/teacher/schedules/{id}/attendance` | GET | 某节课考勤列表 |
| `/api/teacher/schedules/{id}/attendance` | POST | 批量提交考勤 |
| `/api/teacher/attendance/{id}` | PUT | 修改单个学生考勤 |

**考勤状态枚举：**
- `PRESENT` - 出勤
- `LATE` - 迟到
- `LEAVE_EARLY` - 早退
- `ABSENT` - 缺勤
- `SICK_LEAVE` - 病假
- `PERSONAL_LEAVE` - 事假
- `MAKEUP` - 补课

**验收标准：**
- [ ] 进入考勤页默认展示该班级所有学生
- [ ] 支持一键全部设为出勤
- [ ] 可单独修改某个学生考勤状态
- [ ] 考勤数据写入 `edu_attendance` 表
- [ ] 考勤结果自动触发课时扣除逻辑

---

#### Task 6：课时扣除

**目标：** 考勤完成后批量扣减课时，支持异常处理。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/schedules/{id}/deduct`
  - `TeacherApiService`：实现课时扣除逻辑
- 前端：
  - 在考勤详情页集成"确认扣课"按钮

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/schedules/{id}/deduct` | POST | 按课程批量扣减课时 |
| `/api/teacher/lesson-hour-records` | GET | 课时流水查询 |

**课时扣除规则：**
- 出勤（PRESENT）：正常扣减 `schedule.lesson_hours`
- 迟到（LATE）：正常扣减（可配置）
- 病假/事假（已审批）：不扣减
- 缺勤（ABSENT）：正常扣减（可配置）
- 补课（MAKEUP）：不扣减

**写入表：**
- `edu_lesson_hour_record`（流水）
- `edu_lesson_hour_account`（账户余额更新）

**验收标准：**
- [ ] 考勤确认后自动计算应扣课时
- [ ] 余额不足时给出提示但不阻断（标记为欠费）
- [ ] 写入课时流水记录，包含操作人、操作前/后余额
- [ ] 支持查看某学生的课时流水

---

### Phase 3：P1 增强功能

#### Task 7：打卡任务管理

**目标：** 发布打卡作业、查看打卡进度、批量点评。

**涉及文件：**
- 后端：
  - `TeacherP1Controller`：添加 `/checkins`, `/checkins/{id}/progress`
  - `TeacherP1Service`：实现打卡逻辑
- 前端：
  - 新建：`miniapp/src/pages/teacher/checkin/list.vue`
  - 新建：`miniapp/src/pages/teacher/checkin/progress.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/checkins` | GET | 打卡任务列表 |
| `/api/teacher/checkins/{id}/progress` | GET | 打卡进度（按学生/按日期） |

**验收标准：**
- [ ] 发布作业时可选择"启用打卡"并设置周期
- [ ] 老师可查看每个学生的打卡完成天数
- [ ] 支持对打卡批量点评

---

#### Task 8：教学资料上传

**目标：** 老师上传资料、提交审核、配置可见范围。

**涉及文件：**
- 后端：
  - `TeacherP1Controller`：添加 `/materials`
  - `TeacherP1Service`：实现资料逻辑
  - `TeacherP1Mapper`：添加资料 SQL
- 前端：
  - 新建：`miniapp/src/pages/teacher/material/list.vue`
  - 新建：`miniapp/src/pages/teacher/material/upload.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/materials` | GET | 我上传的资料列表 |
| `/api/teacher/materials` | POST | 上传资料 |
| `/api/teacher/materials/{id}` | GET | 资料详情 |
| `/api/teacher/materials/{id}` | PUT | 修改资料 |
| `/api/teacher/materials/{id}/submit-audit` | POST | 提交审核 |

**资料上传字段：**
- 标题、描述
- 分类 ID
- 文件 ID（先调用通用上传接口 `/api/files/upload`）
- 资源类型：PDF / VIDEO / AUDIO / IMAGE
- 可见范围：CAMPUS / CLASS / SELF
- 关联班级 IDs（当 visibility=CLASS 时）
- 学习类型：REQUIRED / OPTIONAL

**审核状态流转：**
`DRAFT` -> `PENDING` -> `APPROVED` / `REJECTED`

**验收标准：**
- [ ] 老师可上传 PDF、视频、音频资料
- [ ] 上传后默认状态为 DRAFT
- [ ] 提交审核后状态变为 PENDING
- [ ] 审核通过后学生端可见
- [ ] 可见范围按配置正确过滤

---

#### Task 9：通知中心

**目标：** 老师端接收系统通知、作业提交提醒、考勤提醒。

**涉及文件：**
- 后端：
  - `TeacherP1Controller`：添加 `/notifications`
- 前端：
  - 新建：`miniapp/src/pages/teacher/notifications.vue`

**关键接口：**

| 接口 | 方法 | 用途 |
|-----|------|------|
| `/api/teacher/notifications` | GET | 通知列表 |
| `/api/teacher/notifications/{id}/read` | POST | 标记已读 |

**验收标准：**
- [ ] 学生提交作业后老师收到通知
- [ ] 通知支持已读/未读状态
- [ ] 首页待办数与通知联动

---

### Phase 4：P2 精细化功能

#### Task 10：老师端个人中心

**目标：** 我的信息、切换校区、退出登录。

**涉及文件：**
- 前端：
  - 新建：`miniapp/src/pages/teacher/mine.vue`

**验收标准：**
- [ ] 展示老师姓名、头像、校区
- [ ] 支持退出登录

---

#### Task 11：数据统计与报表

**目标：** 班级作业完成率、出勤率、课时消耗统计。

**涉及文件：**
- 后端：
  - `TeacherController`：添加 `/statistics/*`
- 前端：
  - 新建：`miniapp/src/pages/teacher/statistics.vue`

**验收标准：**
- [ ] 按班级统计本月出勤率
- [ ] 按班级统计作业提交率
- [ ] 数据可视化展示（简易图表）

---

#### Task 12：评语库

**目标：** 常用评语保存、快捷复用。

**涉及文件：**
- 数据库：新增 `edu_teacher_comment_template` 表
- 后端：
  - `TeacherController`：添加 `/comment-templates`
- 前端：
  - 批改页集成评语库选择

**验收标准：**
- [ ] 老师可保存常用评语
- [ ] 批改时可快捷选择评语
- [ ] 按使用频次排序

---

## 四、前端页面清单

参考学生端 `pages/student/*` 风格：

| 页面 | 路径 | 说明 |
|-----|------|------|
| 老师首页 | `pages/teacher/home` | 仪表盘（今日课程、待办、快捷统计） |
| 班级列表 | `pages/teacher/class/list` | 我的班级 |
| 班级详情 | `pages/teacher/class/detail` | 班级信息+学生列表 |
| 学生档案 | `pages/teacher/student/profile` | 学生详情（课时、出勤、作业） |
| 作业列表 | `pages/teacher/homework/list` | 我发布的作业 |
| 发布作业 | `pages/teacher/homework/create` | 作业发布表单 |
| 作业详情 | `pages/teacher/homework/detail` | 作业统计+提交列表 |
| 批改作业 | `pages/teacher/homework/comment` | 文字+语音点评 |
| 打卡任务 | `pages/teacher/checkin/list` | 打卡作业列表 |
| 打卡进度 | `pages/teacher/checkin/progress` | 学生打卡明细 |
| 考勤列表 | `pages/teacher/attendance/list` | 今日课程考勤入口 |
| 考勤详情 | `pages/teacher/attendance/detail` | 某节课考勤+扣课 |
| 资料列表 | `pages/teacher/material/list` | 我上传的资料 |
| 上传资料 | `pages/teacher/material/upload` | 资料上传表单 |
| 通知中心 | `pages/teacher/notifications` | 消息列表 |
| 我的 | `pages/teacher/mine` | 个人中心 |

### TabBar 调整建议

当前 `pages.json` 中老师端只有 `pages/teacher/home` 一个页面，无 TabBar。建议老师端也采用底部导航：

```json
"tabBar": {
  "list": [
    { "pagePath": "pages/teacher/home", "text": "首页" },
    { "pagePath": "pages/teacher/class/list", "text": "班级" },
    { "pagePath": "pages/teacher/homework/list", "text": "作业" },
    { "pagePath": "pages/teacher/mine", "text": "我的" }
  ]
}
```

注意：由于学生端和老师端共用一个小程序，TabBar 配置需要根据登录身份动态切换。当前 `pages.json` 是静态配置，需要在 `App.vue` 的 `onLaunch` 中根据身份做路由重定向。

---

## 五、数据库变更评估

### 5.1 已有表可直接复用（无需变更）

| 模块 | 已有表 | 评估 |
|-----|--------|------|
| 老师身份 | `edu_teacher`, `sys_user`, `sys_user_role` | 直接复用 |
| 班级 | `edu_class`, `edu_class_student` | 直接复用 |
| 课表 | `edu_class_schedule` | 直接复用 |
| 作业 | `edu_homework`, `edu_homework_target`, `edu_homework_attachment`, `edu_homework_submission`, `edu_homework_submission_file`, `edu_homework_comment`, `edu_homework_checkin` | 直接复用 |
| 考勤 | `edu_attendance` | 直接复用 |
| 课时 | `edu_lesson_hour_account`, `edu_lesson_hour_record` | 直接复用 |
| 资料 | `res_file`, `res_material`, `res_material_class`, `res_category` | 直接复用 |
| 通知 | `sys_notification` | 直接复用 |

### 5.2 建议新增表/字段

#### P2 新增表：`edu_teacher_comment_template`

```sql
CREATE TABLE IF NOT EXISTS edu_teacher_comment_template (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  campus_id BIGINT NOT NULL REFERENCES sys_campus (id),
  teacher_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  usage_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted SMALLINT NOT NULL DEFAULT 0
);
```

#### P1 可选新增字段

- `edu_homework` 表：增加 `allow_resubmit`（是否允许重交）、`resubmit_deadline`（最晚补交时间）
- `edu_attendance` 表：当前字段已足够，无需新增

### 5.3 索引优化建议

| 表 | 建议索引 | 原因 |
|----|---------|------|
| `edu_homework` | `(campus_id, teacher_id, status)` | 老师查询自己发布的作业 |
| `edu_homework_submission` | `(campus_id, homework_id, status)` | 按作业查询待点评提交 |
| `edu_attendance` | `(campus_id, schedule_id, status)` | 按课程查询考勤状态 |

### 5.4 数据库变更结论

**核心结论：数据库无需结构性变更即可支撑 P0 和 P1 功能。**

已有表设计已经充分考虑了老师端的需求：
- `edu_homework.teacher_id` 已支持按老师发布作业
- `edu_class_schedule.teacher_id` 已支持按老师查询课表
- `res_material.owner_teacher_id` 已支持资料归属老师
- `res_material.audit_status` 已支持审核流程

仅需在 P2 阶段按需新增评语库表，其余功能完全基于现有表实现。

---

## 六、关键设计决策总结

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 后端包结构 | 新建 `com.community.edu.teacher` | 与学生端平行，职责清晰 |
| 接口前缀 | `/api/teacher/*` | 与学生端 `/api/student/*` 对称 |
| Scope 机制 | `TeacherScopeService` 仿 `StudentScopeService` | 保持架构一致性 |
| Mapper 模式 | 注解 SQL（`@Select/@Insert`） | 与现有 `StudentMiniappMapper` 一致 |
| 前端页面组织 | `pages/teacher/*` | 与学生端 `pages/student/*` 对称 |
| TabBar | 老师端独立 4 个 Tab | 首页/班级/作业/我的 |
| 数据库变更 | P0/P1 不变更，P2 按需新增 | 现有 schema 已充分覆盖 |

---

## 七、开发顺序建议

```
Phase 1（基础设施）
  -> Task 1: TeacherScopeService + TeacherContext
  -> TeacherController 骨架
  -> TeacherMiniappMapper 基础查询

Phase 2（P0 核心）
  -> Task 2: 首页仪表盘
  -> Task 3: 班级与学生管理
  -> Task 4: 作业管理（发布+列表+批改）
  -> Task 5: 考勤管理
  -> Task 6: 课时扣除

Phase 3（P1 增强）
  -> Task 7: 打卡任务
  -> Task 8: 教学资料上传
  -> Task 9: 通知中心

Phase 4（P2 精细化）
  -> Task 10: 个人中心
  -> Task 11: 数据统计
  -> Task 12: 评语库
```

每个 Phase 建议按"后端接口 -> 前端页面 -> 联调测试"的顺序推进，确保前后端契约一致。
