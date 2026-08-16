# Deployment Guide

Three ways to run this API: **Local** (no Docker — fastest for development), **Docker** (docker-compose — closest to production, self-contained), and **Render** (the actual production deployment). Pick one based on what you're doing.

For how to *test* whichever environment you bring up (curl, Swagger UI, Postman, `mvn test`), see [TESTING.md](TESTING.md).

---

## A. Local (no Docker)

Runs the app directly on your machine against a local Postgres install. Uses the `dev` Spring profile (`application-dev.yml`).

### Prerequisites
- Java 21
- Maven
- PostgreSQL 16 running locally on port 5432

### 1. Create the database and role

```bash
sudo -u postgres psql -c "CREATE USER socialmedia_user WITH PASSWORD 'socialmedia_password';"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db OWNER socialmedia_user;"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db_dev OWNER socialmedia_user;"
```

(`socialmedia_db_dev` is used by the running app under the `dev` profile; `socialmedia_db` is used by `mvn test` — see [TESTING.md](TESTING.md).)

### 2. (Optional) Seed reference data

`schema.sql`/`data.sql` under `src/main/resources/database/` are manual-reference/seed scripts — Spring Boot's own SQL init does **not** run them automatically (only Docker's Postgres init does, see section B). To seed them locally yourself:

```bash
psql -h localhost -U socialmedia_user -d socialmedia_db_dev \
  -f src/main/resources/database/schema.sql \
  -f src/main/resources/database/data.sql
```

Not required to start the app — `dev` profile uses `ddl-auto: create-drop`, so Hibernate creates the schema itself on startup (and drops it on shutdown). Only do this if you want the sample users from `data.sql` (all use password `password123`).

### 3. Run

```bash
mvn clean install
mvn spring-boot:run
```

Defaults to the `dev` profile (see `application.yml` / `SPRING_PROFILES_ACTIVE`). App listens on **http://localhost:8080**.

### 4. Verify

```bash
curl http://localhost:8080/api/v1/posts
```

Should return `[]` (or seeded posts, if you loaded `data.sql`). Swagger UI: http://localhost:8080/swagger-ui.html.

---

## B. Docker (docker-compose)

Runs the packaged app + a real Postgres container together, using the `prod` profile against a SQL-file-initialized schema — closest thing to production you can run locally.

### Prerequisites
- Docker + Docker Compose
- Port 5432 free, or see the note below if your host already runs Postgres on it

### 1. Bring up the stack

```bash
docker compose up -d --build
```

This builds `Dockerfile` (two-stage: `eclipse-temurin:21-jdk-alpine` build → `eclipse-temurin:21-jre-alpine` runtime, non-root `spring` user) and starts:
- `postgres` — `postgres:16-alpine`, seeded via `schema.sql`/`data.sql` mounted into `docker-entrypoint-initdb.d/` (Postgres's own init mechanism runs these on first container startup only, on an empty data volume — unlike Spring Boot's SQL init, which does not run them, per section A above)
- `app` — the built image, `SPRING_PROFILES_ACTIVE=prod`, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (validates the Hibernate-mapped schema against what `schema.sql` created — see the gotcha at the bottom of this doc for why this specific env var matters), datasource pointed at the `postgres` service

The app waits for Postgres's healthcheck before starting (`depends_on: condition: service_healthy`).

### 2. Verify

```bash
curl http://localhost:8080/api/v1/explore
curl http://localhost:8080/api/v1/search/users?query=john
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "password123"}'
```

`data.sql` seeds `john_doe`/`jane_smith`/`alex_wong`/`maria_garcia`/`sam_lee`/`admin`, all with password `password123`. This exact flow (explore, search, login against seeded users) has been run end-to-end and returns correct `200`s.

### 3. Tear down

```bash
docker compose down        # stop, keep the Postgres volume (data persists)
docker compose down -v     # stop and delete the Postgres volume (next `up` reseeds from schema.sql/data.sql)
```

### Port conflict with a host Postgres

If your host already runs Postgres on 5432 (likely — see section A), `docker compose up` will fail to bind `5432:5432`. **Don't** add a `docker-compose.override.yml` with a remapped port — Compose *concatenates* `ports:` lists across `docker-compose.yml` + an override file rather than replacing them, so the conflicting `5432:5432` mapping still gets attempted. Either stop your host Postgres first, or run standalone containers on a throwaway Docker network instead of touching `docker-compose.yml`.

---

## C. Render (production)

This is how **https://socialmedia-api-gsvh.onrender.com** is actually deployed: a Render **Web Service** built from this repo's `Dockerfile`, plus a separate Render **PostgreSQL** instance.

### 1. Create the database

Render dashboard → **New → PostgreSQL**. Once provisioned, open its **Info** page and note the individual connection fields (hostname, port, database name, username, password) — you'll map these to `DB_*` env vars below, not the combined connection-string form.

### 2. Create the web service

Render dashboard → **New → Web Service** → connect this GitHub repo → **Environment: Docker** (Render auto-detects the root `Dockerfile`). Leave build/start commands empty — the `Dockerfile`'s own `ENTRYPOINT` handles it, and it already bakes in `ENV SPRING_PROFILES_ACTIVE=prod`.

### 3. Set environment variables

| Variable | Required? | Value |
|---|---|---|
| `DB_HOST` | **Required** | From the Postgres Info page (bare hostname, not a URL) |
| `DB_PORT` | **Required** | Usually `5432` |
| `DB_NAME` | **Required** | From the Postgres Info page |
| `DB_USER` | **Required** | From the Postgres Info page |
| `DB_PASSWORD` | **Required** | From the Postgres Info page |
| `JWT_SECRET` | **Required** | A real secret, ≥256 bits — **not** the `your-secret-key-...` placeholder from `.env.example` |
| `JWT_EXPIRATION` | Optional | Defaults to `86400000` (24h, ms) |
| `JPA_HIBERNATE_DDL_AUTO` | Optional | Defaults to `update` — see note below |
| `DB_POOL_SIZE` | Optional | Defaults to `20` |
| `LOG_LEVEL` | Optional | Defaults to `INFO` |

`PORT` is injected automatically by Render — don't set it yourself; `server.port` already resolves as `${PORT:${SERVER_PORT:8080}}` to pick it up.

`application-prod.yml`'s datasource URL appends `?sslmode=require` itself — do **not** also add `sslNegotiation=direct` to any of these values; that was a wrong fix for a different (Neon-style SNI-proxy) provider and breaks Render's own managed Postgres (`SSL error: Unsupported or unrecognized SSL message`).

### 4. Deploy and verify

Render builds the Docker image and deploys automatically on push (or on manual "Deploy latest commit"). Once live:

```bash
curl https://socialmedia-api-gsvh.onrender.com/api/v1/posts
```

Swagger UI: https://socialmedia-api-gsvh.onrender.com/swagger-ui.html

### Note: `JPA_HIBERNATE_DDL_AUTO` defaults to `update`

`application-prod.yml` sets `spring.jpa.hibernate.ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}` — safe to leave unset in Render; it falls back to `update`, which lets Hibernate auto-apply additive schema changes (new tables/columns) on boot rather than just validating. This is a deliberate choice for this deployment (over the stricter `validate`, which only checks the schema matches and never modifies it) — if you'd rather Render *never* auto-modify the schema, set `JPA_HIBERNATE_DDL_AUTO=validate` explicitly.

This default exists at all because an *unset placeholder with no default* previously failed silently rather than loudly: Spring's placeholder resolution here is lenient (`resolvePlaceholders`, not `resolveRequiredPlaceholders`), so a bare `${JPA_HIBERNATE_DDL_AUTO}` with nothing supplying it doesn't crash the app at boot — it leaves the literal unresolved string in place, which Hibernate doesn't recognize as any valid `ddl-auto` action and silently treats as a no-op, skipping schema management entirely. Verified directly by booting the `prod` profile with the var omitted before any default existed.

Note `update` never drops or narrows columns, so it won't undo a destructive migration (e.g. a column rename or type change) — those still need a manual `ALTER` against the Render Postgres instance.

---

## Live Deployment Reference

**Live URL:** https://socialmedia-api-gsvh.onrender.com
**Status:** ✅ Active on Render.com
**Database:** PostgreSQL on Render

Render's free tier spins the service down after ~15 min of inactivity; the first request after that takes 30-60s (cold start) while it spins back up, subsequent requests are fast.

For the full endpoint list, use Swagger UI (`/swagger-ui.html`) or the Postman collection (`postman/`) — both are always in sync with the live routes, unlike a hand-maintained list in this doc. For copy-pasteable example requests, see [QUICK_START.md](QUICK_START.md).
