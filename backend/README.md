# 小区英语组班机构后端

Spring Boot 3.x 后端首期工程，已接入 PostgreSQL、Redis、MyBatis-Plus、Flyway、Spring Security、JWT、RBAC 与多校区数据隔离。

## 本地环境

- JDK 17
- Maven 3.9.x
- PostgreSQL: `localhost:5432/edu_group`
- Redis: `localhost:16379`
- 数据库账号: `postgres/postgres`

## 启动

如果 Maven 仍识别到旧 JDK，请先在当前 PowerShell 设置：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

```powershell
mvn -s C:\Users\QB\.m2\settings.xml spring-boot:run
```

如果当前 PowerShell 还未刷新 Maven PATH，可使用：

```powershell
& 'D:\Program Files\apache-maven-3.9.16\bin\mvn.cmd' -s C:\Users\QB\.m2\settings.xml spring-boot:run
```

## 日志输出

后端使用 Spring Boot 默认主流日志方案 `SLF4J + Logback`，配置文件为 `src/main/resources/logback-spring.xml`。

默认日志目录为启动工作目录下的 `logs`。如果从 `D:\workspace\edu\backend` 启动，日志会写入 `D:\workspace\edu\backend\logs`；如果从项目根目录或 IDE 启动，相对路径会跟随对应的工作目录变化。

建议本地固定指定日志目录：

```powershell
$env:LOG_PATH='D:\workspace\edu\backend\logs'
mvn -s C:\Users\QB\.m2\settings.xml spring-boot:run
```

日志文件按天区分并按大小滚动：

```text
logs/edu-backend.2026-06-04.0.log
logs/edu-backend-error.2026-06-04.0.log
```

## 编译

```powershell
mvn -s C:\Users\QB\.m2\settings.xml -DskipTests package
```

仅做代码编译验证可执行：

```powershell
mvn -s C:\Users\QB\.m2\settings.xml -DskipTests compile
```

## 初始化账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 超级管理员 |
| campus_admin | 123456 | 阳光校区管理员 |
| teacher_amy | 123456 | 老师 |
| teacher_bob | 123456 | 老师 |
| parent_li | 123456 | 家长 |
| parent_wang | 123456 | 家长 |

## 核心接口

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/admin/campuses`
- `GET /api/admin/users`
- `GET /api/admin/roles`
- `GET /api/admin/roles/permission-tree`
- `GET /api/admin/teachers`
- `GET /api/admin/courses`
- `GET /api/admin/classes`

除登录和刷新外，请求需携带：

```http
Authorization: Bearer <accessToken>
X-Campus-Id: 1001
```

超级管理员可切换 `X-Campus-Id`，校区管理员、老师和家长只能访问授权校区。
