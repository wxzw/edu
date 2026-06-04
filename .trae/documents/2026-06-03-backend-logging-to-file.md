# 后端通用日志文件输出 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Spring Boot 后端添加 Logback 文件日志输出，包含控制台彩色日志、全量文件日志、ERROR 单独文件，按天+大小滚动，traceId 贯穿全链路。

**Architecture:** 新建 `logback-spring.xml` 作为唯一日志配置，定义 CONSOLE / FILE / ERROR_FILE 三个 Appender，通过 `springProperty` 读取 application.yml 中的 `logging.file.path`，与现有 `@Slf4j` + MDC traceId 无缝集成。

**Tech Stack:** Spring Boot 3.3.5, Logback (内置), SLF4J, Java 17, Maven

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `backend/src/main/resources/logback-spring.xml` | **新建** | Logback 日志配置（Appender、格式、滚动策略、Logger 级别） |
| `backend/src/main/resources/application.yml` | **修改** | 新增 `logging.file.path` 配置项 |
| `backend/.gitignore` | **不改（已存在）** | 确认 `logs/` 在忽略列表中 |

---

### Task 1: 修改 application.yml 增加日志文件路径配置

**Files:**
- Modify: `d:\workspace\edu\backend\src\main\resources\application.yml`

- [ ] **Step 1: 在 logging 节点下增加 file.path**

修改 `d:\workspace\edu\backend\src\main\resources\application.yml` 中 `logging` 部分，增加 `file.path: logs`：

```yaml
logging:
  file:
    path: logs
  level:
    root: info
    com.community.edu: debug
    org.springframework.security: info
```

原有 `logging.level` 三行保持不变，仅在 `logging` 和 `level` 之间插入两行。

- [ ] **Step 2: 确认修改后的 application.yml 格式正确**

使用文本编辑器打开 `d:\workspace\edu\backend\src\main\resources\application.yml`，确认 YAML 缩进正确（2 空格），`file` 与 `level` 同级。

---

### Task 2: 新建 logback-spring.xml 配置文件

**Files:**
- Create: `d:\workspace\edu\backend\src\main\resources\logback-spring.xml`

- [ ] **Step 1: 创建 logback-spring.xml 并写入完整配置**

创建文件 `d:\workspace\edu\backend\src\main\resources\logback-spring.xml`，写入以下内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="false" debug="false">

    <!-- ============================================================ -->
    <!-- 从 Spring Environment 读取配置属性                             -->
    <!-- ============================================================ -->
    <springProperty scope="context" name="LOG_PATH" source="logging.file.path" defaultValue="logs"/>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name" defaultValue="edu-backend"/>

    <!-- ============================================================ -->
    <!-- 日志格式定义                                                   -->
    <!-- ============================================================ -->
    <!-- 控制台彩色格式：日期 级别[traceId] 线程 logger : 消息 -->
    <property name="CONSOLE_LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} %clr([%5level]){faint} [%X{traceId}] %clr([%15.15thread]){magenta} %clr(%-40.40logger{39}){cyan} : %m%n%wEx"/>

    <!-- 文件格式（无颜色标记）：使用 --- 分隔符便于日志平台解析 -->
    <property name="FILE_LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%5level] [%X{traceId}] --- [%thread] %-40.40logger{39} : %m%n%wEx"/>

    <!-- 日志文件字符集 -->
    <property name="FILE_CHARSET" value="UTF-8"/>

    <!-- ============================================================ -->
    <!-- 1) CONSOLE APPENDER - 控制台彩色输出                           -->
    <!-- ============================================================ -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>${FILE_CHARSET}</charset>
        </encoder>
    </appender>

    <!-- ============================================================ -->
    <!-- 2) FILE APPENDER - 全量日志文件（INFO 及以上）                   -->
    <!-- ============================================================ -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>${FILE_CHARSET}</charset>
        </encoder>
        <file>${LOG_PATH}/${APP_NAME}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/history/%d{yyyy-MM-dd}/${APP_NAME}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- ============================================================ -->
    <!-- 3) ERROR_FILE APPENDER - ERROR 级别单独文件                     -->
    <!-- ============================================================ -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>${FILE_CHARSET}</charset>
        </encoder>
        <file>${LOG_PATH}/${APP_NAME}-error.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/history/%d{yyyy-MM-dd}/${APP_NAME}-error.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>90</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- ============================================================ -->
    <!-- Logger 配置：框架日志降噪 + 业务包 DEBUG                        -->
    <!-- ============================================================ -->

    <!-- 框架日志降噪 -->
    <logger name="com.baomidou.mybatisplus" level="WARN"/>
    <logger name="org.mybatis" level="WARN"/>
    <logger name="org.mybatis.spring" level="WARN"/>
    <logger name="org.apache.catalina" level="WARN"/>
    <logger name="org.apache.coyote" level="WARN"/>
    <logger name="org.apache.tomcat" level="WARN"/>
    <logger name="org.springframework.boot.autoconfigure" level="INFO"/>

    <!-- 业务包 DEBUG 级别 -->
    <logger name="com.community.edu" level="DEBUG"/>

    <!-- 安全框架 INFO 级别 -->
    <logger name="org.springframework.security" level="INFO"/>

    <!-- ============================================================ -->
    <!-- Root Logger                                                  -->
    <!-- ============================================================ -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>

</configuration>
```

---

### Task 3: 编译验证

**Files:**
- 无文件变更（仅验证）

- [ ] **Step 1: 执行 Maven 编译**

```bash
cd d:\workspace\edu\backend
mvn clean compile -DskipTests
```

**Expected:** `BUILD SUCCESS`，编译通过。

- [ ] **Step 2: 确认 logback-spring.xml 被复制到 target/classes**

```bash
dir d:\workspace\edu\backend\target\classes\logback-spring.xml
```

**Expected:** 文件存在（`target\classes\logback-spring.xml`）。

- [ ] **Step 3: 确认 application.yml 被复制并包含新配置**

使用 `grep` 或文本搜索确认 `target\classes\application.yml` 中包含 `logging.file.path` 或 `file:` 行：

```bash
Select-String -Path 'd:\workspace\edu\backend\target\classes\application.yml' -Pattern 'file:'
```

**Expected:** 显示 `    file:` 相关行。

---

### Task 4: 启动验证 — 功能测试

- [ ] **Step 1: 启动 Spring Boot 应用**

在 IDE 或命令行启动应用（按项目现有方式启动 `EduBackendApplication`）。

**Expected:** 应用正常启动，控制台出现彩色日志输出，每条日志包含 `[traceId值]` 字段。

- [ ] **Step 2: 验证日志文件生成**

检查项目运行目录下（通常为 `d:\workspace\edu\backend\`）是否生成了 `logs\` 目录及以下文件：

```bash
dir d:\workspace\edu\backend\logs\
```

**Expected:**
- `logs\edu-backend.log` 存在且不为空
- `logs\edu-backend-error.log` 存在（可能为空或只有启动阶段的 ERROR）

- [ ] **Step 3: 验证日志文件内容格式**

打开 `logs\edu-backend.log`，确认日志格式为：

```
2026-06-03 10:15:30.123 [ INFO] [traceId值] --- [线程名] logger名称 : 日志消息
```

**Expected:** 
- 无乱码（UTF-8 编码正确）
- 无颜色转义字符（`%clr` 相关标记不会出现在文件中）
- `[traceId值]` 字段存在（空白表示无活跃请求的日志，如启动日志）
- `---` 分隔符出现

- [ ] **Step 4: 发起 HTTP 请求并验证 traceId 贯穿**

```bash
curl -X POST http://localhost:8055/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"wrong","password":"wrong","campusId":1}'
```

**Expected:** 请求返回后，检查响应头 `X-Trace-Id`，然后在 `logs\edu-backend.log` 中搜索该 traceId，确认对应的日志行包含相同的 traceId，且日志级别为 `[ WARN]`（对应 AuthService 中 `log.warn("login failed...")`）。

- [ ] **Step 5: 模拟 ERROR 场景验证错误日志分离**

**验证方法 A（推荐）：**
查找 `GlobalExceptionHandler` 中 `log.error("unexpected server error", ex)` 的触发条件（未知异常），暂时在某个 Controller 中添加一个会抛 RuntimeException 的测试端点，调用该端点后确认：
- `logs\edu-backend.log` 包含该 ERROR 行
- `logs\edu-backend-error.log` 包含该 ERROR 行

**验证方法 B（无需改代码）：**
观察启动过程中或调用已知会出错的接口，确认 ERROR 级别日志同时出现在全量日志和错误日志中。

---

### Task 5: .gitignore 确认

**Files:**
- 查看: `d:\workspace\edu\.gitignore`

- [ ] **Step 1: 确认 logs/ 在 .gitignore 中**

```bash
Select-String -Path 'd:\workspace\edu\.gitignore' -Pattern 'logs'
```

**Expected:** 如果 `logs/` 或 `logs` 已在忽略列表中，跳过此步骤。
如果不存在，在 `.gitignore` 末尾追加 `logs/` 一行：

```
logs/
```

---

### Task 6: Commit

- [ ] **Step 1: 提交所有变更**

```bash
cd d:\workspace\edu
git add backend/src/main/resources/logback-spring.xml backend/src/main/resources/application.yml
git diff --cached --stat
git commit -m "feat(backend): add Logback file logging with daily rolling and traceId support
- Add logback-spring.xml with CONSOLE, FILE (INFO+), ERROR_FILE appenders
- Configure SizeAndTimeBasedRollingPolicy: 50MB per file, 30d retention, 5GB cap
- ERROR logs retained 90 days with 1GB cap
- Log format includes timestamp, level, traceId (MDC), thread, logger, message
- Console output uses colored pattern for development
- Suppress noisy framework logs (MyBatis-Plus, Tomcat, etc.)"
```

**Expected:** Commit 成功，变更包含 1 个新文件和 1 个修改文件。

---

## 附：目录结构预期

启动后生成的日志文件结构：

```
backend/
  logs/
    edu-backend.log               ← 当前全量日志（INFO 及以上）
    edu-backend-error.log         ← 当前 ERROR 日志
    history/
      2026-06-03/
        edu-backend.0.log         ← 归档的全量日志块
        edu-backend.1.log
        edu-backend-error.0.log   ← 归档的 ERROR 日志块
      2026-06-02/
        edu-backend.0.log
        ...
```

## 附：关键设计决策

| 决策 | 理由 |
|------|------|
| 使用 `logback-spring.xml` 而非编程式配置 | Spring Boot 标准实践，支持 `<springProperty>` 读取 application.yml，支持 `<springProfile>` 按环境区分（预留扩展） |
| 文件格式中保留 `[%X{traceId}]` | 与现有 `TraceIdHolder`（MDC key=`traceId`）一致，已在 `TraceIdFilter` 中自动设置/清除 |
| 全量日志 ThresholdFilter=INFO | 与 root level=INFO 一致，排除 DEBUG/TRACE 避免文件增长过快 |
| ERROR 单独文件保留 90 天 | ERROR 日志价值高，用于回溯历史问题 |
| `cleanHistoryOnStart=true` | Spring Boot 默认行为，启动时清理超限旧文件 |
| 控制台保留 `%clr()` 彩色 | 开发友好，文件已排除颜色标记不会产生乱码 |

## 附：生产环境后续优化建议（不在本计划范围内）

1. **JSON 格式输出**：如需接入 ELK/Loki 等日志平台，可添加 `net.logstash.logback:logstash-logback-encoder` 依赖并配置 JSON Appender
2. **异步日志**：高并发场景下可包裹 `AsyncAppender`（队列 1024, `discardingThreshold=0`）
3. **按环境区分策略**：可添加 `<springProfile name="prod">` 块配置更长的保留时间或更大的 totalSizeCap
4. **Sentinel 日志告警**：配置 `logback-sentry` 或 `logback-alert` 实现 ERROR 日志实时通知
