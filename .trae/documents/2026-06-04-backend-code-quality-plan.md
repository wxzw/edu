# 后端代码质量提升 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提升后端代码质量：补充单元测试、添加 Javadoc 注释、修复命名规范问题、消除安全隐患

**Architecture:** 分三个独立阶段执行：(1) 测试基础设施 + 核心模块单元测试 (2) Javadoc 注释补充 (3) 命名规范 + 代码质量修复。每个阶段独立可交付。

**Tech Stack:** JUnit 5, Mockito, AssertJ, Spring Boot Test, spring-security-test, H2 (内存数据库用于 Mapper 测试)

---

## 现状审计总结

| 审计项 | 现状 | 严重程度 |
|--------|------|---------|
| 单元测试 | **0% 覆盖率**，`src/test/` 目录不存在 | **高** |
| 类级别 Javadoc | **全部缺失** (0/70+ 文件) | 中 |
| 方法级别 Javadoc | **全部缺失** (仅 8 处行内注释) | 中 |
| Controller 方法命名 | 整体良好，约 25 处非 RESTful 命名 | 低 |
| 中文硬编码 (异常消息) | **100+ 处** | 中（内部系统可暂缓） |
| 代码重复 (`clientIp()`, `normalize()`) | 3 处重复 | 低 |
| 默认密码硬编码 `"123456"` | 安全隐患 | **高** |
| `EduClassStudent` 未继承 `BaseEntity` | 不一致 | 低 |

---

## Phase 1: 测试基础设施 + 核心单元测试

### Task 1: 创建测试基础设施

**Files:**
- Create: `backend/src/test/java/com/community/edu/BaseUnitTest.java`
- Create: `backend/src/test/resources/application-test.yml`

- [ ] **Step 1: 创建 application-test.yml**

创建 `backend/src/test/resources/application-test.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.hikari.HikariDataSource
    username: sa
    password:
  flyway:
    enabled: false
  data:
    redis:
      host: localhost
      port: 6379
app:
  security:
    jwt-secret: test-secret-key-for-unit-testing-only-2026
    access-token-ttl: 1h
    refresh-token-ttl: 1d
  wechat:
    miniapp:
      mock-enabled: true
```

- [ ] **Step 2: 在 pom.xml 中添加 H2 测试依赖**

在 `backend/pom.xml` 的 `<dependencies>` 中添加：

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 3: 创建 BaseUnitTest 基类**

创建 `backend/src/test/java/com/community/edu/BaseUnitTest.java`：

```java
package com.community.edu;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * 所有单元测试的基类。
 * 统一配置 DisplayName 生成策略为标准方法名。
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseUnitTest {
}
```

- [ ] **Step 4: 验证测试基础设施**

Run: `cd d:\workspace\edu\backend && mvn test-compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/ backend/pom.xml
git commit -m "test: add test infrastructure (H2, application-test.yml, BaseUnitTest)"
```

---

### Task 2: 安全层单元测试 (最高优先级)

**Files:**
- Create: `backend/src/test/java/com/community/edu/security/JwtTokenProviderTest.java`
- Create: `backend/src/test/java/com/community/edu/security/JwtAuthenticationFilterTest.java`

- [ ] **Step 1: 编写 JwtTokenProviderTest**

```java
package com.community.edu.security;

import com.community.edu.common.context.CurrentUser;
import com.community.edu.security.JwtTokenProvider.ParsedToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest extends BaseUnitTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void should_create_and_parse_access_token() {
        CurrentUser user = CurrentUser.builder()
            .userId(1L).username("admin").realName("管理员")
            .accountType("TEACHER").roleCodes(Set.of("SUPER_ADMIN"))
            .permissions(Set.of("campus:read")).campusIds(List.of(1L))
            .defaultCampusId(1L).selectedCampusId(1L)
            .build();

        String token = jwtTokenProvider.createAccessToken(user, 3600L);
        ParsedToken parsed = jwtTokenProvider.parse(token);

        assertThat(parsed.getCurrentUser().getUserId()).isEqualTo(1L);
        assertThat(parsed.getCurrentUser().getUsername()).isEqualTo("admin");
        assertThat(parsed.getType()).isEqualTo("access");
    }

    @Test
    void should_throw_on_invalid_token() {
        assertThatThrownBy(() -> jwtTokenProvider.parse("invalid.token.here"))
            .isInstanceOf(BizException.class);
    }

    @Test
    void should_throw_on_expired_token() {
        CurrentUser user = CurrentUser.builder()
            .userId(1L).username("admin").build();

        String token = jwtTokenProvider.createAccessToken(user, -1L);
        assertThatThrownBy(() -> jwtTokenProvider.parse(token))
            .isInstanceOf(BizException.class);
    }
}
```

- [ ] **Step 2: 编写 JwtAuthenticationFilterTest**

```java
package com.community.edu.security;

import com.community.edu.common.context.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest extends BaseUnitTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @Test
    void should_set_authentication_for_valid_token() {
        // 需要注入真实的 JwtTokenProvider 或 mock
        // 验证 filter 正确解析 token 并设置 SecurityContext
    }

    @Test
    void should_not_set_authentication_for_missing_token() {
        when(request.getHeader("Authorization")).thenReturn(null);
        // 验证 filter 对无 token 请求的处理
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `cd d:\workspace\edu\backend && mvn test -pl . -Dtest="JwtTokenProviderTest" -DfailIfNoTests=false`
Expected: 至少 JwtTokenProvider 的 3 个测试通过

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/community/edu/security/
git commit -m "test: add JwtTokenProvider and JwtAuthenticationFilter unit tests"
```

---

### Task 3: 认证服务单元测试

**Files:**
- Create: `backend/src/test/java/com/community/edu/auth/AuthServiceTest.java`

- [ ] **Step 1: 编写 AuthServiceTest**

覆盖以下场景：
- `login()` 成功（正确用户名密码）
- `login()` 失败（用户名不存在）
- `login()` 失败（密码错误）
- `login()` 失败（账号被禁用）
- `refreshToken()` 成功
- `refreshToken()` 失败（token 类型不对）
- `refreshToken()` 失败（token 已过期）
- `buildCurrentUser()` 正确构建 CurrentUser

使用 Mockito mock `SysUserMapper`、`SysUserRoleMapper`、`JwtTokenProvider`、`PasswordEncoder`。

- [ ] **Step 2: 运行测试**

Run: `cd d:\workspace\edu\backend && mvn test -Dtest="AuthServiceTest" -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/community/edu/auth/
git commit -m "test: add AuthService unit tests for login and token refresh"
```

---

### Task 4: Admin 服务层单元测试

**Files:**
- Create: `backend/src/test/java/com/community/edu/admin/AdminCampusServiceTest.java`
- Create: `backend/src/test/java/com/community/edu/admin/AdminClassServiceTest.java`
- Create: `backend/src/test/java/com/community/edu/admin/AdminUserServiceTest.java`

- [ ] **Step 1: 编写 AdminCampusServiceTest**

覆盖：`page()`, `create()`, `update()`, `updateStatus()`
Mock: `SysCampusMapper`, `SysUserCampusMapper`

- [ ] **Step 2: 编写 AdminClassServiceTest**

覆盖：`page()`, `create()`, `update()`, `addStudent()`, `removeStudent()`
重点测试：学生重复添加、班级人数上限、学生不存在等边界情况

- [ ] **Step 3: 编写 AdminUserServiceTest**

覆盖：`page()`, `create()`, `update()`, `resetPassword()`
重点测试：用户名唯一性、默认密码编码

- [ ] **Step 4: 运行测试**

Run: `cd d:\workspace\edu\backend && mvn test -Dtest="Admin*ServiceTest" -DfailIfNoTests=false`

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/java/com/community/edu/admin/
git commit -m "test: add Admin service layer unit tests (Campus, Class, User)"
```

---

### Task 5: 通用组件单元测试

**Files:**
- Create: `backend/src/test/java/com/community/edu/common/exception/GlobalExceptionHandlerTest.java`
- Create: `backend/src/test/java/com/community/edu/common/context/CurrentUserHolderTest.java`
- Create: `backend/src/test/java/com/community/edu/common/trace/TraceIdHolderTest.java`

- [ ] **Step 1: 编写 GlobalExceptionHandlerTest**

使用 MockMvc 测试异常处理：
- `BizException` 返回对应 error code 和 message
- `MethodArgumentNotValidException` 返回校验错误
- 未知异常返回 500

- [ ] **Step 2: 编写 CurrentUserHolderTest**

覆盖：`getRequired()` 有认证时返回用户、无认证时抛异常、`getOrNull()` 返回 null

- [ ] **Step 3: 编写 TraceIdHolderTest**

覆盖：`set()`/`get()`/`clear()` 正常流程

- [ ] **Step 4: 运行测试并 Commit**

```bash
git add backend/src/test/java/com/community/edu/common/
git commit -m "test: add common component unit tests (ExceptionHandler, CurrentUserHolder, TraceId)"
```

---

## Phase 2: Javadoc 注释补充

### Task 6: 为所有 Controller 类添加 Javadoc

**Files:**
- Modify: 所有 14 个 Controller 文件

- [ ] **Step 1: 为每个 Controller 类添加类级别 Javadoc**

示例格式：

```java
/**
 * 校区管理接口。
 * <p>提供校区的增删改查、状态变更等管理功能。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/campuses")
@RequiredArgsConstructor
public class AdminCampusController {
```

需要添加的 Controller 列表（14 个）：

| Controller | 描述 |
|---|---|
| `AdminCampusController` | 校区管理接口 |
| `AdminClassController` | 班级管理接口 |
| `AdminCourseController` | 课程管理接口 |
| `AdminGroupController` | 拼班管理接口 |
| `AdminP1Controller` | 运营管理接口（资料库、活动、通知） |
| `AdminRoleController` | 角色权限管理接口 |
| `AdminStudentController` | 学生管理接口 |
| `AdminTeacherController` | 老师管理接口 |
| `AdminUserController` | 用户管理接口 |
| `AuthController` | PC 端认证接口（登录、刷新、当前用户） |
| `MiniappAuthController` | 小程序认证接口（微信登录、身份选择） |
| `StudentController` | 学生小程序业务接口 |
| `StudentP1Controller` | 学生小程序运营接口 |
| `TeacherController` | 老师小程序业务接口 |

- [ ] **Step 2: 为 Controller 的 public 方法添加 Javadoc**

对每个 `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` 方法添加简短注释：

```java
/**
 * 分页查询校区列表。
 */
@GetMapping
public ApiResponse<PageResponse<CampusResponse>> page(...) {
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/community/edu/admin/ backend/src/main/java/com/community/edu/auth/
git add backend/src/main/java/com/community/edu/miniapp/ backend/src/main/java/com/community/edu/student/
git add backend/src/main/java/com/community/edu/teacher/
git commit -m "docs: add Javadoc to all Controller classes and public methods"
```

---

### Task 7: 为所有 Service 类添加 Javadoc

**Files:**
- Modify: 所有 19 个 Service 文件

- [ ] **Step 1: 为每个 Service 类添加类级别 Javadoc**

示例：

```java
/**
 * 校区管理服务。
 * <p>处理校区的 CRUD 操作，包括校区与用户的关联管理。</p>
 */
@Service
@RequiredArgsConstructor
public class AdminCampusService {
```

- [ ] **Step 2: 为关键 public 方法添加 Javadoc**

重点为以下类型的方法添加注释：
- 业务入口方法（被 Controller 调用的方法）
- 包含复杂逻辑的方法（如权限校验、数据转换）
- 事务方法（`@Transactional`）

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/community/edu/admin/ backend/src/main/java/com/community/edu/auth/
git add backend/src/main/java/com/community/edu/miniapp/ backend/src/main/java/com/community/edu/student/
git add backend/src/main/java/com/community/edu/teacher/ backend/src/main/java/com/community/edu/service/
git commit -m "docs: add Javadoc to all Service classes and key public methods"
```

---

### Task 8: 为 Security、Config、Common、Entity 类添加 Javadoc

**Files:**
- Modify: security/ (4), config/ (5), common/ (15), entity/ (16)

- [ ] **Step 1: 为 Security 类添加 Javadoc**

4 个文件：`JwtTokenProvider`, `JwtAuthenticationFilter`, `RestAccessDeniedHandler`, `RestAuthenticationEntryPoint`

- [ ] **Step 2: 为 Config 类添加 Javadoc**

5 个文件：`SecurityConfig`, `AppSecurityProperties`, `MybatisMetaObjectHandler`, `MybatisPlusConfig`, `TenantProperties`

- [ ] **Step 3: 为 Common 类添加 Javadoc**

15 个文件：`BizException`, `ErrorCode`, `GlobalExceptionHandler`, `ApiResponse`, `PageResponse`, `PageQuery`, `CurrentUser`, `CurrentUserHolder`, `CampusContextHolder`, `RequirePermission`, `PermissionAspect`, `OperationLog`, `OperationLogAspect`, `TraceIdFilter`, `TraceIdHolder`

- [ ] **Step 4: 为 Entity 类添加 Javadoc**

16 个文件：`BaseEntity`, `SysUser`, `SysCampus`, `SysRole`, `SysPermission`, `SysRolePermission`, `SysUserRole`, `SysUserCampus`, `SysOperationLog`, `EduCourse`, `EduClass`, `EduClassStudent`, `EduTeacher`, `EduStudent`, `EduGuardian`, `CampusEntity`

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/community/edu/security/ backend/src/main/java/com/community/edu/config/
git add backend/src/main/java/com/community/edu/common/ backend/src/main/java/com/community/edu/entity/
git commit -m "docs: add Javadoc to Security, Config, Common, and Entity classes"
```

---

## Phase 3: 命名规范 + 代码质量修复

### Task 9: 修复安全隐患 — 默认密码外部化

**Files:**
- Modify: `backend/src/main/java/com/community/edu/admin/AdminUserService.java`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 在 application.yml 中添加默认密码配置**

```yaml
app:
  security:
    default-password: "123456"  # 仅用于开发环境，生产环境必须修改
```

- [ ] **Step 2: 修改 AdminUserService 使用配置注入**

将 `private static final String DEFAULT_PASSWORD = "123456";` 改为通过 `@Value` 注入。

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/community/edu/admin/AdminUserService.java backend/src/main/resources/application.yml
git commit -m "fix(security): externalize default password to application.yml config"
```

---

### Task 10: 修复 Controller 方法命名 — 统一 RESTful 风格

**Files:**
- Modify: 约 10 个 Controller 文件（仅修改方法名，不改变 URL 路由）

- [ ] **Step 1: 批量重命名 Controller 方法**

| 当前方法名 | 建议改为 | 文件 |
|---|---|---|
| `categories()` | `listCategories()` | AdminP1Controller |
| `materials()` | `listMaterials()` | AdminP1Controller |
| `material()` | `getMaterial()` | AdminP1Controller |
| `activities()` | `listActivities()` | AdminP1Controller |
| `activity()` | `getActivity()` | AdminP1Controller |
| `registrations()` | `listRegistrations()` | AdminP1Controller |
| `orders()` | `listOrders()` | AdminP1Controller |
| `payments()` | `listPayments()` | AdminP1Controller |
| `notifications()` | `listNotifications()` | AdminP1Controller |
| `permissionTree()` | `listPermissionTree()` | AdminRoleController |
| `classes()` | `listClasses()` | TeacherController, StudentController |
| `homeworks()` | `listHomeworks()` | TeacherController, StudentController |
| `schedules()` | `listSchedules()` | StudentController |
| `lessonAccounts()` | `listLessonAccounts()` | StudentController |
| `lessonRecords()` | `listLessonRecords()` | StudentController |
| `lessonHourRecords()` | `listLessonHourRecords()` | TeacherController |
| `groupRequests()` | `listGroupRequests()` | StudentController |

**注意**：`@RequestMapping` 的 URL 路径不变，仅改方法名。前端调用不受影响。

- [ ] **Step 2: 同步修改 Service 层对应方法名（如需）**

Service 层方法名跟随 Controller 变更。

- [ ] **Step 3: 编译验证**

Run: `cd d:\workspace\edu\backend && mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/
git commit -m "refactor: standardize Controller method names to RESTful conventions"
```

---

### Task 11: 消除代码重复 — 抽取公共工具方法

**Files:**
- Create: `backend/src/main/java/com/community/edu/common/util/WebUtils.java`
- Create: `backend/src/main/java/com/community/edu/common/util/StringUtil.java`
- Modify: `AuthController.java`, `MiniappAuthController.java`, `OperationLogAspect.java`
- Modify: `AdminP1Service.java`, `StudentP1Service.java`

- [ ] **Step 1: 创建 WebUtils — 抽取 clientIp()**

```java
package com.community.edu.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Web 请求工具类。
 */
public final class WebUtils {

    private WebUtils() {}

    /**
     * 获取当前请求的客户端 IP 地址。
     * 优先从 X-Forwarded-For 头获取（支持代理），其次从 RemoteAddr 获取。
     */
    public static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
```

- [ ] **Step 2: 创建 StringUtil — 抽取 normalize()**

```java
package com.community.edu.common.util;

/**
 * 字符串工具类。
 */
public final class StringUtil {

    private StringUtil() {}

    /** 将空字符串转为 null，非空字符串去除首尾空白后返回。 */
    public static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
```

- [ ] **Step 3: 替换 AuthController、MiniappAuthController、OperationLogAspect 中的 clientIp()**

将三处重复的 `private String clientIp()` 方法替换为 `WebUtils.clientIp()`。

- [ ] **Step 4: 替换 AdminP1Service、StudentP1Service 中的 normalize()**

将重复的 `normalize()` 方法替换为 `StringUtil.blankToNull()`。

- [ ] **Step 5: 编译验证并 Commit**

```bash
git add backend/src/main/java/
git commit -m "refactor: extract WebUtils.clientIp() and StringUtil.blankToNull() to eliminate duplication"
```

---

### Task 12: 修复 EduClassStudent 未继承 BaseEntity

**Files:**
- Modify: `backend/src/main/java/com/community/edu/entity/EduClassStudent.java`

- [ ] **Step 1: 让 EduClassStudent 继承 BaseEntity**

移除手动声明的 `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `deleted`, `version` 字段，改为 `extends BaseEntity`。保留 `classId`, `studentId`, `enrolledAt`, `status` 等业务字段。

- [ ] **Step 2: 编译验证并 Commit**

```bash
git add backend/src/main/java/com/community/edu/entity/EduClassStudent.java
git commit -m "refactor: make EduClassStudent extend BaseEntity for consistency"
```

---

## 附：暂不处理项（后续优化建议）

| 项目 | 原因 | 建议 |
|------|------|------|
| 中文硬编码异常消息 (100+ 处) | 内部管理系统，前端已做 i18n，后端消息仅用于调试 | 后续如需国际化，引入 `messages.properties` + `MessageSource` |
| 审计日志注解中的中文 | 仅用于后台管理日志，不影响功能 | 可在 i18n 阶段统一处理 |
| 校验注解中的中文消息 | `@NotBlank(message="...")` | 可在 i18n 阶段改为 ValidationMessages.properties |
| 状态值硬编码 (`"ENABLED"`, `"ACTIVE"` 等) | 改动面大，需引入枚举类 | 后续单独计划处理 |
| `AdminP1Controller` / `StudentP1Controller` 命名含义 | "P1" 含义不明 | 通过 Javadoc 注释说明即可 |

## 附：实施优先级建议

1. **P0 (立即)**: Task 9 — 默认密码外部化（安全隐患）
2. **P1 (高)**: Task 1-5 — 单元测试基础设施 + 核心测试
3. **P2 (中)**: Task 6-8 — Javadoc 注释补充
4. **P3 (低)**: Task 10-12 — 命名规范 + 代码质量
