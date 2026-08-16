# Testing Guide

Four different ways to exercise this API, depending on what you're trying to verify. See [DEPLOYMENT.md](DEPLOYMENT.md) for how to bring up each environment referenced below.

| Method | Good for | Needs |
|---|---|---|
| [`mvn test`](#1-automated-integration-tests-mvn-test) | Regression checks, CI, verifying a code change didn't break anything | Local Postgres (no Docker) |
| [curl](#2-manual-curl) | Quick one-off checks, scripting, debugging a specific request/response | Any running instance (local, Docker, or live) |
| [Swagger UI](#3-interactive-swagger-ui) | Exploring endpoints interactively, trying requests without writing code | A browser, any running instance |
| [Postman](#4-postman-collection) | Repeatable manual test flows, chained requests (register → login → post → …), sharing a test suite with others | Postman app, any running instance |

---

## 1. Automated integration tests (`mvn test`)

There is no test profile and no embedded/H2 database — `@SpringBootTest` integration tests boot the full Spring context against a **real local Postgres**, using the default (no-profile) `application.yml` datasource (`socialmedia_db`, not `socialmedia_db_dev`).

### Setup (one-time)

```bash
sudo -u postgres psql -c "CREATE USER socialmedia_user WITH PASSWORD 'socialmedia_password';"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db OWNER socialmedia_user;"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db_dev OWNER socialmedia_user;"
```

### Run

```bash
mvn test                                    # full suite
mvn test -Dtest=ClassName                   # one test class
mvn test -Dtest=ClassName#methodName        # one test method
```

### Things that catch people out

- **Same database as local dev, not a separate test schema.** Integration tests only clean up in `@BeforeEach` (before their own run), not after — so after any test run, `socialmedia_db` ends up containing whatever the *last* test class in the run seeded (e.g. `SearchControllerIntegrationTest`'s `john_doe`/`jane_smith`/`bob_johnson`, with the literal string `"hashed"` as their password — not a real bcrypt hash, so nothing logs in as them). If `SELECT * FROM users` looks wrong after a test run, this is why.
- To reload real seed data after that: clear leftover rows, then `psql ... -f schema.sql -f data.sql`.
- `schema.sql`/`data.sql` are **not** auto-run by `mvn test` — the schema comes from Hibernate `ddl-auto`, per the default `application.yml`.

---

## 2. Manual curl

Point the same requests at whichever environment you have running — only the base URL changes.

| Environment | Base URL |
|---|---|
| Local (section A) | `http://localhost:8080` |
| Docker (section B) | `http://localhost:8080` |
| Render (production) | `https://socialmedia-api-gsvh.onrender.com` |

Minimal smoke test (works unauthenticated on any environment):

```bash
curl <base_url>/api/v1/posts
curl <base_url>/api/v1/explore
```

Full register → login → authenticated-request walkthrough: see [QUICK_START.md](QUICK_START.md) (written against the live URL, but every command works against `http://localhost:8080` too — just swap the base URL).

Against Docker specifically, the seeded `data.sql` users (`john_doe`, `jane_smith`, `alex_wong`, `maria_garcia`, `sam_lee`, `admin` — all password `password123`) let you skip registration and log in directly:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "password123"}'
```

---

## 3. Interactive (Swagger UI)

| Environment | URL |
|---|---|
| Local / Docker | http://localhost:8080/swagger-ui.html |
| Render (production) | https://socialmedia-api-gsvh.onrender.com/swagger-ui.html |

1. Hit an unauthenticated endpoint (e.g. `GET /api/v1/posts` → **Try it out** → **Execute**) to confirm the server responds.
2. `POST /api/v1/auth/login` with a known user's credentials → copy the `token` field from the response.
3. Click **Authorize** (top right, padlock icon) → paste the token (no `Bearer ` prefix needed, Swagger adds it) → **Authorize**.
4. Every endpoint tagged with a lock icon now runs authenticated automatically.

Endpoints are grouped by feature (`1-Authentication` … `8-Search`) via the dropdown next to the top-left title, matching the Postman collection's folder layout.

---

## 4. Postman collection

Full setup, auth model, variable reference, and endpoint-by-endpoint auth table live in [`postman/README.md`](postman/README.md) — this section is just the short version.

1. Import `postman/socialmedia-api.postman_collection.json`.
2. Import an environment: `postman/socialmedia-api-environment.json` (dev, `http://localhost:8080`) or `postman/socialmedia-api-production.json` (prod, live Render URL) — select it from the environment dropdown, top-right.
3. Run **Authentication → Register User**, then **Login User**. Their Tests scripts auto-save `jwt_token`/`user_id`/`username` into the environment; every other request inherits the token automatically via collection-level Bearer auth.
4. From there, follow the folder order (Users → Posts → Comments → Likes → Follow → Feed & Timeline → Search). `Delete Post`/`Delete Comment` are placed last in their folders deliberately — they invalidate `post_id`/`comment_id` for the requests above them, so don't run the collection top-to-bottom unattended.

Switching the environment dropdown between dev and prod re-targets every request at that environment's `base_url` — nothing else needs editing.
