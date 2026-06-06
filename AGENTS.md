# AGENTS.md

Guidance for Codex and other AI coding agents working in this repository.

## Project Overview

This repository is a community education management system with three main apps:

- `backend/`: Spring Boot 3.3, Java 17, Spring Security, MyBatis-Plus, Flyway, PostgreSQL, Redis.
- `frontend/`: Vue 3 admin console using Vite, TypeScript, Pinia, Vue Router, Element Plus, Axios.
- `miniapp/`: uni-app Vue 3 WeChat Mini Program using TypeScript and Pinia.

Important support files:

- `backend/src/main/resources/application.yml`: local backend config.
- `backend/src/main/resources/db/migration/`: Flyway migrations and seed data.
- `frontend/src/api/http.ts`: admin HTTP client.
- `miniapp/src/api/http.ts`: miniapp HTTP client.
- `miniapp/src/manifest.json`: uni-app manifest, including WeChat Mini Program AppID.
- `miniapp/dist/dev/mp-weixin/`: generated WeChat DevTools project. This is build output.

## Local Commands

Backend:

```bash
cd backend
mvn spring-boot:run
mvn test
mvn package -DskipTests
```

Admin frontend:

```bash
cd frontend
npm run dev
npm run typecheck
npm run build
```

Miniapp:

```bash
cd miniapp
npm run dev:mp-weixin
npm run build:mp-weixin
npm run typecheck
```

If the shell cannot find global `npm`, inspect the workspace runtime paths available to Codex and prefer the bundled Node executable plus the project's local `node_modules/.bin` scripts.

## Ports And Runtime

- Backend local port: `8055`.
- Admin frontend dev port: `5173`.
- Admin frontend preview port: `4173`.
- Docker Compose maps backend as host `8056` to container `8055`.
- PostgreSQL default local port: `5432`, database `edu_group`.
- Redis default local port: `6379`.
- Swagger UI: `http://localhost:8055/swagger-ui.html`.

The current backend local config has:

- `app.wechat.miniapp.mock-enabled: true`
- empty `app-id` and `app-secret` placeholders
- JWT and database defaults intended for local development only

## Miniapp And Real Device Debugging

For iOS or Android real-device debugging, never use `localhost` as the miniapp API base URL. On a phone, `localhost` means the phone itself, not the developer machine.

Use a local environment override in `miniapp/.env.local`:

```bash
VITE_API_BASE_URL=http://<developer-machine-lan-ip>:8055
```

Then rebuild/regenerate the WeChat Mini Program output:

```bash
cd miniapp
npm run dev:mp-weixin
```

Open `miniapp/dist/dev/mp-weixin` in WeChat DevTools after rebuilding. The generated `dist/dev/mp-weixin/api/http.js` must contain the LAN URL, not `http://localhost:8055`.

Known LAN IP candidates from prior debugging were:

- `192.168.3.23`
- `192.168.3.35`

Verify from the phone first:

```text
http://<developer-machine-lan-ip>:8055/swagger-ui.html
```

If the phone cannot open Swagger while the computer can, check Windows Firewall, router/client isolation, and WeChat's iOS "Local Network" permission.

In WeChat DevTools, enable the local debugging settings that skip request domain / TLS / HTTPS certificate checks for development. These settings are only for local development.

## WeChat AppID And AppSecret Rules

- The miniapp AppID can live in `miniapp/src/manifest.json` under `mp-weixin.appid`.
- The known project AppID is `wxae0e749151644db0`.
- AppSecret is a backend-only secret. Never put it in `miniapp/`, `frontend/`, `manifest.json`, generated WeChat project files, screenshots, logs, or committed docs.
- For real WeChat login, configure the secret through backend environment variables or deployment secret management, then set `app.wechat.miniapp.mock-enabled=false`.
- If an AppSecret is exposed in chat, logs, or Git history, recommend rotating it in the WeChat public platform.

For local mock login, AppSecret is not required. The current mock login path sends `mockPhone` / `mockOpenId` to `/api/miniapp/auth/login`.

Mock phone examples:

- Teacher: `13900000011`
- Student: `13900000201`
- Guardian: `13900000101`

## Code Conventions

- Keep changes narrowly scoped. Do not do broad rewrites unless the user explicitly asks.
- Work with the existing architecture:
  - Backend controllers return `ApiResponse`.
  - Backend business logic lives in service classes.
  - Backend data access uses mapper classes and MyBatis conventions already present.
  - Admin frontend API calls should go through existing API/http helpers.
  - Miniapp API calls should go through `miniapp/src/api/http.ts`.
  - Miniapp auth and identity state lives in `miniapp/src/stores/auth.ts`.
- Preserve multi-campus behavior. Requests often depend on `X-Campus-Id`, `X-Identity-Type`, `X-Identity-Id`, and sometimes `X-Student-Id`.
- Avoid committing generated output such as `miniapp/dist/`, `frontend/dist/`, `backend/target/`, logs, uploads, and local env files.
- Do not revert user changes in the working tree unless explicitly asked.

## Encoding Notes

Some existing Chinese text may display as mojibake in PowerShell `Get-Content`, even when the underlying file can still be valid UTF-8. When JSON parsing or display looks suspicious, validate with Node reading `utf8`, or inspect the file through tooling that preserves UTF-8.

For new agent-facing docs, prefer ASCII where practical. Keep paths, commands, ports, and configuration keys exact.

## Verification Checklist

Before finishing a change, run the smallest relevant checks:

- Backend-only changes: `cd backend && mvn test` when feasible.
- Admin frontend changes: `cd frontend && npm run typecheck` and `npm run build` when feasible.
- Miniapp changes: `cd miniapp && npm run typecheck`; rebuild with `npm run dev:mp-weixin` or `npm run build:mp-weixin` when generated WeChat output matters.
- Real-device miniapp login fixes: confirm generated `dist/dev/mp-weixin/api/http.js` contains the LAN API URL and no `localhost`.

If a check cannot run because dependencies, Docker, network, or local services are unavailable, report that clearly with the exact command attempted.
