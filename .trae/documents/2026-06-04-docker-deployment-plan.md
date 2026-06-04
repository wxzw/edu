# 后端 Docker 容器化部署 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Spring Boot 后端应用创建 Dockerfile 和 docker-compose.yml，实现一键打包部署到 Docker 容器

**Architecture:** 使用多阶段构建 Dockerfile（Maven 构建 → JRE 运行），docker-compose 编排后端 + PostgreSQL + Redis 服务，支持环境变量注入配置

**Tech Stack:** Docker, Docker Compose, Maven, OpenJDK 17, PostgreSQL 15, Redis 7

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `backend/Dockerfile` | **新建** | 多阶段构建：Stage 1 Maven 编译 → Stage 2 JRE 运行 |
| `backend/.dockerignore` | **新建** | 排除不需要复制到镜像的文件 |
| `docker-compose.yml` | **新建** (项目根目录) | 编排后端、PostgreSQL、Redis 三个服务 |
| `backend/src/main/resources/application-docker.yml` | **新建** | Docker 环境的 Spring Boot 配置 |
| `.env` | **新建** (项目根目录) | 环境变量配置文件（数据库密码等敏感信息） |

---

### Task 1: 创建 Dockerfile

**Files:**
- Create: `d:\workspace\edu\backend\Dockerfile`

- [ ] **Step 1: 编写多阶段构建 Dockerfile**

创建 `d:\workspace\edu\backend\Dockerfile`：

```dockerfile
# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制 pom.xml 下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="edu-backend"
LABEL description="Community English class management backend"

# 安装必要的工具
RUN apk add --no-cache curl

# 创建非 root 用户运行应用
RUN addgroup -S edu && adduser -S edu -G edu

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

# 创建日志目录并设置权限
RUN mkdir -p /app/logs && chown -R edu:edu /app

USER edu

# 暴露端口
EXPOSE 8055

# JVM 参数（可通过环境变量覆盖）
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
ENV SPRING_PROFILES_ACTIVE=docker

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8055/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

- [ ] **Step 2: 创建 .dockerignore**

创建 `d:\workspace\edu\backend\.dockerignore`：

```
# 构建输出
target/
logs/
*.log

# IDE
.idea/
*.iml

# Git
.git/
.gitignore

# 测试相关（构建阶段不需要）
src/test/

# 文档
README.md
*.md

# 本地配置
application-local.yml
application-dev.yml
```

---

### Task 2: 创建 application-docker.yml

**Files:**
- Create: `d:\workspace\edu\backend\src\main\resources\application-docker.yml`

- [ ] **Step 1: 编写 Docker 环境配置**

创建 `d:\workspace\edu\backend\src\main\resources\application-docker.yml`：

```yaml
server:
  port: 8055

spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:edu_group}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    hikari:
      pool-name: edu-hikari
      maximum-pool-size: 15
      minimum-idle: 3
      connection-timeout: 30000
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
      database: ${REDIS_DB:0}
      timeout: 3s
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 2
    locations: classpath:db/migration
  jackson:
    default-property-inclusion: non_null
    time-zone: Asia/Shanghai

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

app:
  security:
    jwt-secret: ${JWT_SECRET:change-me-in-production}
    access-token-ttl: 2h
    refresh-token-ttl: 7d
    default-password: ${DEFAULT_PASSWORD:Test@123456}
  tenant:
    header-name: X-Campus-Id
  wechat:
    miniapp:
      app-id: ${WECHAT_APP_ID:}
      app-secret: ${WECHAT_APP_SECRET:}
      mock-enabled: ${WECHAT_MOCK_ENABLED:true}

logging:
  file:
    path: /app/logs
  level:
    root: info
    com.community.edu: debug
    org.springframework.security: info

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

---

### Task 3: 创建 docker-compose.yml

**Files:**
- Create: `d:\workspace\edu\docker-compose.yml`

- [ ] **Step 1: 编写 docker-compose.yml**

创建 `d:\workspace\edu\docker-compose.yml`：

```yaml
version: "3.8"

services:
  # ============================================================
  # PostgreSQL 数据库
  # ============================================================
  postgres:
    image: postgres:15-alpine
    container_name: edu-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: edu_group
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD:-postgres}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d edu_group"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    networks:
      - edu-network

  # ============================================================
  # Redis 缓存
  # ============================================================
  redis:
    image: redis:7-alpine
    container_name: edu-redis
    restart: unless-stopped
    command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 5s
    networks:
      - edu-network

  # ============================================================
  # 后端应用
  # ============================================================
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: edu-backend
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: edu_group
      DB_USER: postgres
      DB_PASSWORD: ${DB_PASSWORD:-postgres}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_DB: 0
      JWT_SECRET: ${JWT_SECRET:-edu-group-backend-docker-secret-change-me}
      DEFAULT_PASSWORD: ${DEFAULT_PASSWORD:-Test@123456}
      WECHAT_APP_ID: ${WECHAT_APP_ID:-}
      WECHAT_APP_SECRET: ${WECHAT_APP_SECRET:-}
      WECHAT_MOCK_ENABLED: ${WECHAT_MOCK_ENABLED:-true}
      JAVA_OPTS: >-
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -XX:InitialRAMPercentage=50.0
        -Djava.security.egd=file:/dev/./urandom
    volumes:
      - backend_logs:/app/logs
    ports:
      - "8055:8055"
    networks:
      - edu-network

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
  backend_logs:
    driver: local

networks:
  edu-network:
    driver: bridge
```

---

### Task 4: 创建 .env 文件

**Files:**
- Create: `d:\workspace\edu\.env`

- [ ] **Step 1: 编写 .env 模板**

创建 `d:\workspace\edu\.env`：

```bash
# ============================================================
# 数据库配置
# ============================================================
DB_PASSWORD=postgres

# ============================================================
# JWT 密钥（生产环境必须修改！）
# ============================================================
JWT_SECRET=edu-group-backend-docker-secret-change-me

# ============================================================
# 默认密码（新建用户时的默认密码）
# ============================================================
DEFAULT_PASSWORD=Test@123456

# ============================================================
# 微信小程序配置（可选）
# ============================================================
WECHAT_APP_ID=
WECHAT_APP_SECRET=
WECHAT_MOCK_ENABLED=true
```

---

### Task 5: 验证 Docker 构建

- [ ] **Step 1: 确认 Docker 环境**

```bash
docker --version
docker-compose --version
```

Expected: Docker version 20.x+ 和 Docker Compose version 2.x+

- [ ] **Step 2: 构建后端镜像**

```bash
cd d:\workspace\edu
docker-compose build backend
```

Expected: 构建成功，无错误

- [ ] **Step 3: 启动所有服务**

```bash
cd d:\workspace\edu
docker-compose up -d
```

Expected: 三个容器都启动成功

- [ ] **Step 4: 验证服务健康**

```bash
# 查看容器状态
docker-compose ps

# 查看后端日志
docker-compose logs -f backend

# 测试健康检查端点
curl http://localhost:8055/actuator/health
```

Expected:
- `docker-compose ps` 显示三个容器状态为 `Up (healthy)`
- 日志显示 `Started EduBackendApplication` 无异常
- curl 返回 `{"status":"UP"}`

- [ ] **Step 5: 测试 API 接口**

```bash
# 测试登录接口
curl -X POST http://localhost:8055/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123456","campusId":1}'
```

Expected: 返回登录成功响应

- [ ] **Step 6: 停止服务**

```bash
cd d:\workspace\edu
docker-compose down
```

---

### Task 6: 添加 Actuator 依赖（用于健康检查）

**Files:**
- Modify: `d:\workspace\edu\backend\pom.xml`

- [ ] **Step 1: 添加 Spring Boot Actuator**

在 `pom.xml` 的 `<dependencies>` 中添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

- [ ] **Step 2: 编译验证**

```bash
cd d:\workspace\edu\backend
mvn clean compile -DskipTests
```

---

### Task 7: Commit

- [ ] **Step 1: 提交所有变更**

```bash
cd d:\workspace\edu
git add backend/Dockerfile backend/.dockerignore backend/src/main/resources/application-docker.yml
git add docker-compose.yml .env backend/pom.xml
git commit -m "feat(deploy): add Docker support with docker-compose orchestration

- Add multi-stage Dockerfile (Maven build + JRE runtime)
- Add docker-compose.yml with PostgreSQL, Redis, and backend services
- Add application-docker.yml for containerized environment
- Add .env template for environment variables
- Add Spring Boot Actuator for health checks
- Configure health checks for all services"
```

---

## 附：常用 Docker 命令速查

| 命令 | 说明 |
|------|------|
| `docker-compose up -d` | 后台启动所有服务 |
| `docker-compose down` | 停止并删除所有服务 |
| `docker-compose down -v` | 停止并删除服务 + 数据卷 |
| `docker-compose logs -f backend` | 实时查看后端日志 |
| `docker-compose ps` | 查看容器状态 |
| `docker-compose exec backend sh` | 进入后端容器 |
| `docker-compose build --no-cache backend` | 强制重新构建后端镜像 |
| `docker system prune -f` | 清理未使用的镜像和容器 |

## 附：生产环境注意事项

1. **修改 JWT_SECRET**: `.env` 中的 `JWT_SECRET` 必须改为强随机字符串
2. **修改 DB_PASSWORD**: 数据库密码必须改为强密码
3. **关闭 mock 模式**: 微信小程序配置需要填入真实的 `app-id` 和 `app-secret`
4. **日志持久化**: `backend_logs` 卷已配置，日志文件在容器内的 `/app/logs`
5. **数据库备份**: 定期备份 `postgres_data` 卷
6. **资源限制**: 生产环境建议为每个服务添加 `deploy.resources.limits`
