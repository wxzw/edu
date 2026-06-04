# 小程序报错自动修复计划

> **目标：** 逐项修复 uni-app 小程序中所有已识别的报错和类型问题，确保所有页面能正常运行。
> **原理：** 在本次计划制定前，已用 Explore agent 完成了全代码库的静态分析，识别出所有潜在问题。AI 按优先级逐项自动修复，无需人工逐个排查。

---

## 一、当前状态分析

对 `d:\workspace\edu\miniapp` 进行了全面静态审查，发现以下问题：

| 优先级 | 问题数 | 严重程度 |
|--------|--------|----------|
| P0 阻断性 | 1 (11处) | 所有 teacher 页面数据获取完全失效 |
| P1 类型错误 | 2 (5处) | 类型定义冲突、泛型参数错误 |
| P2 逻辑缺陷 | 3 (14处) | requireIdentity 未检查、未使用 import、回退逻辑 |
| P3 优化 | 2 | Map 响应性、资料 API 缺失 |

---

## 二、分步修复计划

### 修复 1：P0 阻断 - 修正所有 teacher 页面的数据获取模式

**根因：** `http.ts` 的 `request<T>` 函数已解包 `ApiResponse`，直接返回 `T` 类型数据。但所有 teacher 页面错误地使用了 `res.data` 模式访问。

**修改文件（11处）：**

| 文件 | 原代码 | 改为 |
|------|--------|------|
| `pages/teacher/home.vue` L19 | `dashboard.value = res.data;` | `dashboard.value = res;` |
| `pages/teacher/class/list.vue` L15 | `classes.value = res.data;` | `classes.value = res;` |
| `pages/teacher/class/detail.vue` L15 | `classDetail.value = res.data;` | `classDetail.value = res;` |
| `pages/teacher/student/profile.vue` L15 | `profile.value = res.data;` | `profile.value = res;` |
| `pages/teacher/homework/list.vue` L15 | `homeworks.value = res.data;` | `homeworks.value = res;` |
| `pages/teacher/homework/detail.vue` L22 | `homework.value = res.data;` | `homework.value = res;` |
| `pages/teacher/homework/create.vue` L49 | `const homeworkId = res.data.id;` | `const homeworkId = res.id;` |
| `pages/teacher/attendance/list.vue` L15 | `schedules.value = res.data;` | `schedules.value = res;` |
| `pages/teacher/attendance/detail.vue` L16 | `detail.value = res.data;` | `detail.value = res;` |
| `pages/teacher/checkin/list.vue` L15 | `checkins.value = res.data.filter(...)` | `checkins.value = res.filter(...)` |
| `pages/teacher/checkin/progress.vue` L15 | `homework.value = res.data;` | `homework.value = res;` |

---

### 修复 2：P1 类型 - `api.ts` 中 `StudentProfile` 重复定义

**问题：** `StudentProfile` 在第 77 行和第 525 行定义了两次，后者覆盖前者，但两者字段结构完全不同。

**修改文件：** `miniapp/src/types/api.ts`

**修改内容：** 将第 525 行的 `StudentProfile` 重命名为 `TeacherStudentProfile`，避免覆盖 student 端使用的定义。

---

### 修复 3：P1 类型 - `teacher.ts` 中 API 泛型参数错误

**问题：** `getTeacherHomeworks()` 泛型声明为 `HomeworkListItem[]`（学生端类型），实际应声明为 `TeacherHomeworkListItem[]`。`getTeacherHomeworkDetail()` 泛型声明为 `HomeworkDetail`，实际应声明为 `TeacherHomeworkDetail`。

**修改文件：** `miniapp/src/api/teacher.ts`

**修改内容：**
- L48: `request<HomeworkListItem[]>` → `request<TeacherHomeworkListItem[]>`
- L55: `request<HomeworkDetail>` → `request<TeacherHomeworkDetail>`
- 同步修改 import 语句

---

### 修复 4：P2 逻辑 - `requireIdentity` 返回值未检查

**问题：** 所有 teacher 页面调用 `requireIdentity('TEACHER')` 后未检查返回值，身份验证失败后仍发起数据请求。

**修改文件（12处，所有 teacher page 的 onShow/onLoad 回调）：**

将：
```typescript
onShow(() => {
  requireIdentity('TEACHER');
  fetchData();
});
```
改为：
```typescript
onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchData();
});
```

---

### 修复 5：P2 - 移除未使用的 import

**修改文件：** `miniapp/src/pages/teacher/attendance/detail.vue` L6

移除未使用的 `AttendanceStudentItem` import。

---

### 修复 6：P2 - campusShortName 回退逻辑

**修改文件：** `miniapp/src/pages/teacher/home.vue` L72

将 `identity?.campusId`（数字）改为 `'--'` 或 `identity?.displayName`。

---

### 修复 7：P3 可选 - Map 响应性优化

**修改文件：** `miniapp/src/pages/teacher/attendance/detail.vue`

将 `ref<Map<number, string>>(new Map())` 改为 `ref<Record<number, string>>({})`，确保 Vue 模板能正确追踪状态变化。

对应的状态修改从 `studentStatuses.value.set(id, status)` 改为 `studentStatuses.value[id] = status`。

---

## 三、修复顺序

```
修复 1（P0 阻断性）→ 修复 2-3（P1 类型）→ 修复 4-6（P2 逻辑）→ 修复 7（P3 优化）
```

先修复阻断性 bug，再修复类型错误，然后处理逻辑缺陷，最后做响应性优化。

---

## 四、验证方式

修复完成后，逐一验证：

1. **修复 1 验证：** 在 IDE 中打开任意 teacher 页面（如 `home.vue`），检查 `res.data` 模式是否已全部替换为 `res` 模式
2. **修复 2 验证：** 确认 `api.ts` 中只有一个 `StudentProfile` 定义，新加的 teacher 专用类型名为 `TeacherStudentProfile`
3. **修复 3 验证：** 确认 `teacher.ts` 中泛型参数与页面实际使用的类型一致
4. **修复 4 验证：** 确认所有 teacher 页面的 `onShow`/`onLoad` 都在 `requireIdentity` 后有 `if (!...) return` 判断
5. **编译验证：** 在 `miniapp` 目录执行构建命令，确认无 TypeScript 类型错误

---

## 五、关键决策

1. **全部使用现有 `http.ts` 的请求封装**，不修改请求层
2. **所有 teacher 页面与学生页面保持一致的数据获取模式**（直接 `res` 而不使用 `.data`）
3. **类型命名遵循 student/teacher 前缀区分规则**，避免类型覆盖
4. **修复范围仅限于已识别的错误**，不引入新功能或重构
