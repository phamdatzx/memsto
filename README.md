# memsto

Quickly store memes, icons, pictures and quotes in collections, then copy them into chats.
See [plan.md](plan.md) for scope.

| Part | Stack |
|---|---|
| `frontend/` | React 19, Vite, TypeScript, Tailwind, TanStack Query, React Router |
| `backend/` | Spring Boot 4.1 (Java 21, Maven), Spring Security, JPA, Flyway, AWS SDK v2 (S3) |
| DB | MySQL 8.4 |
| Object storage | Cloudflare R2 in prod; SeaweedFS (S3-compatible) locally |

## Run locally

```sh
cp .env.example .env
docker compose -f compose.yaml -f compose.dev.yaml up -d --build --wait
```

- App: http://localhost:5173 (nginx serving the frontend and proxying `/api`)
- API: http://localhost:8080 (`/api/ping`, `/actuator/health`)
- S3: http://localhost:8333 (keys in `infra/seaweedfs/s3.json`)
- MySQL: localhost:3306

### Hot reload

Run only the infrastructure in Docker and the apps on the host:

```sh
docker compose -f compose.yaml -f compose.dev.yaml up -d mysql s3
cd backend && STORAGE_CREATE_BUCKET=true ./mvnw spring-boot:run
cd frontend && npm run dev        # http://localhost:5173 (Vite proxies /api to :8080)
```

## Production

Set the R2 values in `.env` (`S3_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com`, `S3_REGION=auto`,
bucket and API token keys), real DB passwords, and a fresh `JWT_SECRET` (`openssl rand -base64 48`), then:

```sh
docker compose -f compose.yaml -f compose.prod.yaml up -d --build
```

Only nginx is exposed (`HTTP_PORT`, default 80). Put TLS in front of it: the session cookie is `Secure` in prod.

## Auth

- `POST /api/auth/register`, `POST /api/auth/login` set an httpOnly `memsto_session` cookie (HS256 JWT, 30 days).
- `GET /api/auth/me` returns the current user or 401; `POST /api/auth/logout` clears the cookie.
- CSRF: double-submit cookie. The SPA reads `XSRF-TOKEN` and sends it as `X-XSRF-TOKEN` on non-GET requests
  (`frontend/src/lib/api.ts` handles this, priming via `GET /api/auth/csrf`).

## Layout

```
backend/    dev.memsto.{config,storage,common}  db/migration (Flyway)
frontend/   src/lib/api.ts  App.tsx
infra/      nginx/default.conf, seaweedfs/s3.json
compose.yaml + compose.dev.yaml / compose.prod.yaml
```
