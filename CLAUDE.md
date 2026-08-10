# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Meta

Editing this CLAUDE.md file itself never requires asking the user for permission — update it freely whenever project context changes.

**After any change, addition, or fix to the codebase (code, config, scripts, docs, Postman collection, Swagger config, etc.), thoroughly update the relevant section(s) of this file in the same turn — don't wait to be asked.** This includes new conventions discovered, bugs fixed (and why, so they aren't reintroduced), new files/tools added, and any behavior that isn't obvious from just reading the code. Err on the side of updating this file too often rather than too rarely.

## Project status

Seven features implemented end-to-end (entity → repository → service → controller → DTOs → tests): **User Management & Authentication** (register/login/JWT, profile CRUD), **Posts**, **Comments**, **Likes**, **Follow** (follower/following graph via a self-referential `Follow` join entity), **Feed** (personalized feed, per-user timeline, explore page — all paginated), and **Search** (users by username, posts by content — also paginated). Post/comment/like counters and follower/following counts are all maintained via atomic JPQL updates (`PostRepository`/`UserRepository`), not read-modify-write, to avoid lost updates under concurrent requests. Security infrastructure (`SecurityConfig`, JWT filter/provider, route constants, global exception handling) underlies all of it. Docker/`docker-compose` setup exists for containerized deployment (see Docker section below). A Postman collection covering all 31 endpoints exists under `postman/` (see Postman section below).

`AuditData` (`@MappedSuperclass`) now carries `id` (`@Id @GeneratedValue(IDENTITY)`) in addition to `createdAt`/`updatedAt` — every entity extends it rather than declaring its own `id`.

Package layout is feature-based: `com.shishir.socialmedia.<feature>.{entity,repository,dto,service,controller}` (see `user/` and `post/`). Cross-cutting stuff lives in `config/` (`Routes`, `AuditData`, `SecurityConfig`) and `exception/` (custom exceptions, `ExceptionResponse`, `GlobalExceptionHandler`). New features should follow this same package shape.

## Conventions established across features (follow these — each was a real bug once)

- **`Routes.*` constants are already full absolute paths** (e.g. `POST_BY_ID = "/api/v1/posts/{postId}"`). Controllers must **not** also put a `Routes.X` value on a class-level `@RequestMapping` — Spring concatenates class-level + method-level patterns literally, so combining an absolute constant with an absolute class-level prefix 404s the endpoint. Controllers here have no class-level `@RequestMapping`; every `@GetMapping`/`@PostMapping`/etc. takes the full `Routes.*` constant directly.
- **`SecurityConfig` `permitAll()` rules must be `HttpMethod`-scoped.** Sibling routes can share the same path shape (e.g. `USER_BY_USERNAME` and `USER_BY_ID` are both single-segment `/users/{x}` patterns) — an un-scoped `permitAll()` on one leaks onto any other route with the same shape, including write verbs like `PUT`/`DELETE` on what looks like a different logical route. Always pair `permitAll()` with `HttpMethod.GET` (or whichever verb is actually meant to be public).
- **Lombok `@Builder` silently drops field initializers.** Any entity/DTO field with an inline default (`= 0`, `= true`, `= UserRole.USER`, `= new ArrayList<>()`) needs `@Builder.Default`, or any `.builder()...build()` call that omits it gets `null` instead of the intended default.
- **Plain `@Builder` (as opposed to `@SuperBuilder`) doesn't expose superclass fields.** `AuditData`'s `id`/`createdAt`/`updatedAt` aren't buildable via `User.builder()`/`Post.builder()`/etc. — which is fine, since they're always DB/Hibernate-generated and nothing should be setting them via a builder anyway. If a future entity genuinely needs to set an inherited field through its builder, that requires switching that entity (and `AuditData`) to `@SuperBuilder` instead — don't do this without a real need.
- **Don't put `@Data` on JPA `@Entity` classes.** It generates `toString()`/`equals()`/`hashCode()` over every field, including lazy `@ManyToOne`/`@OneToMany` associations (risks `LazyInitializationException` from something as innocuous as a log statement) and sensitive fields (e.g. password hash ending up in logs). Use `@Getter @Setter @ToString(exclude = {...lazy/sensitive fields...})` on the entity plus `@EqualsAndHashCode(callSuper = true)`, since `id`-scoped equals/hashCode (`@EqualsAndHashCode(of = "id")`) now lives once on `AuditData` itself.
- **`AccessDeniedException` (403), not `UnauthorizedException` (401), for ownership/permission checks** — e.g. "you can only edit your own post/profile". `UnauthorizedException` is for actual authentication failures (bad password, missing/invalid token).
- **Public (`permitAll`) response DTOs must not leak email.** `UserProfileResponse`/`PostDetailResponse` include an email field for authenticated "this is you" views; public-facing mapper methods (`mapToPublicProfileResponse`, the post detail mapper) must leave it unset.
- **Resolving the current user:** take `org.springframework.security.core.Authentication` as a controller method parameter (Spring injects it automatically), then call `UserService.getCurrentUserId(authentication.getName())` — don't reach into `SecurityContextHolder` manually in controllers.
- **Hibernate dialect:** use `org.hibernate.dialect.PostgreSQLDialect`. Versioned dialect classes from Hibernate 5 (`PostgreSQL16Dialect` etc.) don't exist in Hibernate 6 (what Spring Boot 3.3.0 uses) and fail app startup with a `ClassNotFoundException` buried under a `BeanCreationException`.
- **Production classes (services, controllers) use constructor injection via `@RequiredArgsConstructor` on final fields — never `@Autowired` field injection.** Test classes are the deliberate exception: they use `@Autowired` field injection, which is the idiomatic, conventional pattern for `@SpringBootTest` classes. Don't convert test classes to constructor injection.
- **A "check" or "list" endpoint whose own description says "the authenticated user" must actually require authentication** — don't permitAll it just because a given/example test calls it with no token. Fix the test to carry a real JWT (`jwtTokenProvider.generateToken(...)`) instead of weakening the endpoint. This has come up repeatedly (`checkIfUserLikedPost`, `checkIfFollowing`).
- **When an endpoint's viewer and its subject can differ (e.g. viewing someone else's timeline), don't conflate them.** Thread the actual authenticated viewer through separately from whichever ID the URL/query param names — passing the wrong one silently computes things like "liked by current user" against the wrong person.
- **`SecurityConfig` must explicitly `permitAll()` the Swagger/OpenAPI paths** (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`) — they aren't covered by anything else and default to requiring a JWT, which makes the API docs themselves inaccessible without already having a token. Caught by actually curling `/v3/api-docs/swagger-config` and getting a 401, not by reading the config.
- **`GroupedOpenApi.Builder` (springdoc 2.0.2) has no `.description(...)` method — only `.displayName(...)`.** Also: a group's `pathsToMatch` must match where the endpoints *actually* live, not where their feature name might suggest. Likes/Follow endpoints are nested under `/api/v1/posts/{postId}/likes/...` and `/api/v1/users/{userId}/follow...` — a naive `/api/v1/likes/**`/`/api/v1/follows/**` pattern matches nothing and silently ships an empty group in the Swagger UI. Verify with `curl localhost:PORT/v3/api-docs/GroupName` and check `paths` isn't empty.

Response codes documented via `@ApiResponse` should reflect what the service layer actually throws, not be copied from a template — e.g. ownership violations (`AccessDeniedException`) are 403, not 401; `unfollowUser` returns `200` with a body, not `204`. Check the relevant `*Service.java` before writing `@ApiResponse` annotations on a new endpoint.
- **Spring Data JPA derived-query method names don't group `Or`/`And` the way they read.** `findByXContainingOrYContainingAndZTrue` parses left-to-right with no parentheses — it means `X OR (Y AND Z)`, not `(X OR Y) AND Z`. If a method mixes `Or` and `And` and the `And` condition is meant to apply to the whole thing (e.g. excluding inactive/deleted rows), write it as an explicit `@Query` with real parentheses instead of trusting the derived name.
- **`<finalName>` must go on the top-level `<build>` element, not inside `spring-boot-maven-plugin`'s `<configuration>`.** The latter is silently ignored (`Parameter 'finalName' is read-only, must not be used in configuration` warning) — caught by actually running the build, not by reading the POM.

## JDK / Lombok

The local JDK is **21** (matches `pom.xml`'s `<java.version>21</java.version>` — build/run with this). The machine also has JDK 11/17/25 installed alongside it, and the default `java`/`JAVA_HOME` on `PATH` in a plain shell may resolve to 25 rather than 21 — if builds behave unexpectedly, check `java -version` first. Both JDK 21 and 25 compile and pass the full test suite here (verified), so this isn't currently a hard blocker either way.

`pom.xml` still pins `lombok.version` to `1.18.44` and explicitly declares `annotationProcessorPaths` for lombok in `maven-compiler-plugin`, even though JDK 21 doesn't strictly need it. Reason: Spring Boot 3.3.0's parent BOM defaults to Lombok `1.18.32`, and JDK 25 (a) doesn't support that old a Lombok build and (b) stopped auto-discovering annotation processors from the plain `-classpath` (only `-processorpath`/`annotationProcessorPaths` is honored). Both fixes are harmless under JDK 21, so leave them in place — don't revert them back to defaults just because 21 is the primary target. If Lombok-generated members (`log`, generated getters/setters/builders) fail to compile with "cannot find symbol" on some other machine/CI, this is the first thing to check.

## Commands

- Build: `mvn clean install`
- Run locally: `mvn spring-boot:run` (defaults to the `dev` profile per `application.yml` / `SPRING_PROFILES_ACTIVE`)
- Run tests: `mvn test`
- Run a single test class: `mvn test -Dtest=ClassName`
- Run a single test method: `mvn test -Dtest=ClassName#methodName`
- Package: `mvn clean package`

## Testing

There is no test profile and no embedded/H2 database — `@SpringBootTest` integration tests boot the full context against the **real local Postgres**, using the default (no-suffix) `application.yml` datasource (`socialmedia_db`, not `socialmedia_db_dev`). Before `mvn test` will pass, that role/database must exist:

```
sudo -u postgres psql -c "CREATE USER socialmedia_user WITH PASSWORD 'socialmedia_password';"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db OWNER socialmedia_user;"
sudo -u postgres psql -c "CREATE DATABASE socialmedia_db_dev OWNER socialmedia_user;"
```

`schema.sql`/`data.sql` under `src/main/resources/database/` are manual-reference/seed scripts only — Spring Boot's SQL init doesn't auto-run them from that path, and the actual test/dev schema comes from Hibernate `ddl-auto`. Seeded dev users in `data.sql` all use password `password123`.

**`mvn test` uses the same database as local dev** (no separate test schema/DB) and integration tests only clean up in `@BeforeEach` (before their own run), not after — so after any test run, the dev database ends up containing whatever the *last* test class in the run seeded (e.g. `SearchControllerIntegrationTest`'s `john_doe`/`jane_smith`/`bob_johnson`, with the literal string `"hashed"` as their password field — not a real bcrypt hash, so nothing logs in as them). If `SELECT * FROM users` looks wrong/incomplete after running tests, this is why — don't assume it's `data.sql`'s seed data without checking `created_at`. Reload the real seed data with `psql ... -f schema.sql -f data.sql` (after clearing any leftover rows) rather than guessing.

## Configuration

Config is split by Spring profile:
- `application.yml` — base config, port 8080, springdoc/swagger UI at `/swagger-ui.html`, hardcoded local Postgres credentials for default/no-profile use.
- `application-dev.yml` — dev overrides: separate `socialmedia_db_dev` database, `ddl-auto: create-drop`, verbose SQL/Hibernate/web logging.
- `application-prod.yml` — prod overrides: all datasource/JWT values pulled from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`), `ddl-auto: validate` (no auto schema changes), error messages/stacktraces suppressed, file-based logging.

Copy `.env.example` to `.env` and fill in real values for local secrets (DB credentials, `JWT_SECRET`). Never commit `.env`. `.env.production` is a template for real deployments (Render.com or similar) — every credential-like field in it (`DB_HOST`, `DB_PASSWORD`) is an obvious `CHANGE_ME_*` placeholder, not a real-looking default, specifically so it can't be mistaken for something safe to ship as-is. It's gitignored like `.env`/`.env.local`.

`server.port` resolves as `${PORT:${SERVER_PORT:8080}}` (in `application.yml`, applies to all profiles). This exists because Render.com and similar PaaS platforms inject their own `PORT` env var at runtime and expect the app to bind to it — Spring Boot's relaxed binding does *not* auto-map `PORT` → `server.port` (only `SERVER_PORT` matches that convention), so without this explicit fallback chain the app would silently listen on the wrong port on such a platform. `application-prod.yml`'s `DB_POOL_SIZE`/`LOG_LEVEL` env vars are wired the same way (`${DB_POOL_SIZE:20}`, `${LOG_LEVEL:INFO}`) rather than being hardcoded — check these three if a deployment's env vars don't seem to be taking effect.

## Docker

`Dockerfile` is a two-stage build (`eclipse-temurin:21-jdk-alpine` → `eclipse-temurin:21-jre-alpine`, runs as a non-root `spring` user). `docker-compose.yml` runs the app against a real `postgres:16-alpine` container, seeded via `schema.sql`/`data.sql` mounted as `docker-entrypoint-initdb.d/*.sql` — unlike Spring Boot's own SQL init (which doesn't run these files, see Testing above), Postgres's *own* init mechanism does run them, on first container startup only (empty data dir). The whole stack — build, Postgres init scripts, `ddl-auto: validate` against the SQL-created schema, JWT signing, and real API responses — has been verified working end-to-end (built and ran the containers, curled `/api/v1/explore`, `/api/v1/search/users`, and `/api/v1/auth/login` against the seeded `data.sql` users, got correct 200s).

If you need to test `docker compose up` locally while the host's own Postgres is already on port 5432 (likely, given the Testing section above), don't add a `ports:`-remapping override file — Compose *concatenates* `ports` lists across `docker-compose.yml` + `docker-compose.override.yml` rather than replacing them, so the base `5432:5432` mapping still gets attempted and still conflicts. Spin up standalone containers on a throwaway Docker network instead (see git history / ask for the exact commands if needed), and don't touch the real `docker-compose.yml`.

`app.commentsCount`/`likesCount`/etc. columns in `schema.sql` and the actual Hibernate-generated dev schema are two independent things that happen to agree — `docker-compose.yml`'s Postgres uses `schema.sql` directly (via Postgres init) with `ddl-auto: validate`, so if entity mappings ever drift from `schema.sql`, this is what would break, invisibly, only in the Docker/prod path and not in local `mvn test` (which uses Hibernate `ddl-auto: update`/`create-drop`, not `schema.sql`, per the Testing section). Keep `schema.sql` in sync by hand whenever an entity's columns change.

## Swagger / OpenAPI

`SwaggerConfig.java` (springdoc 2.0.2) drives `/swagger-ui.html`. It defines the `bearer-jwt` `SecurityScheme` referenced by every controller's `@SecurityRequirement(name = "bearer-jwt")`, a `Local Development`/`Production (Render)` server list, and 8 numbered `GroupedOpenApi` beans (`1-Authentication` … `8-Search`) — the same feature grouping the Postman collection's 8 folders mirror, so keep the two in sync if either changes.

- **A `GlobalOpenApiCustomizer` (`pathOrderCustomizer`) reorders the generated `Paths`** into a fixed list matching the documented manual test flow (Auth → Users → Posts → Comments → Likes → Follow → Feed → Search), because Spring's request-mapping registration order doesn't follow controller source order and springdoc otherwise renders paths in whatever order that produces. If a new route is added, it must be appended to this `order` list too, or it silently falls to the end (via the `original.forEach(ordered::addPathItem)` fallback) instead of its intended position.
- **`GroupedOpenApi.pathsToMatch("/api/v1/feed/**", ...)` for path-only routes with no sub-segments (`/feed`, `/timeline`, `/explore`) is intentional and correct**, not a bug — Spring's `AntPathMatcher` treats a trailing `/**` as matching the base path itself (zero segments) as well as anything deeper, so this still catches the exact `/api/v1/feed` route. Don't "fix" it to a bare `/api/v1/feed` thinking `/**` requires a trailing segment.
- Verify any group change with `curl localhost:PORT/v3/api-docs/<GroupName>` and check `paths` isn't empty (see the `permitAll()`/`GroupedOpenApi` bullets under Conventions above for the failure modes this catches).

## Postman

`postman/socialmedia-api.postman_collection.json` covers all 31 endpoints across 8 folders (Authentication, Users, Posts, Comments, Likes, Follow, Feed & Timeline, Search), plus `socialmedia-api-environment.json` (dev) and `socialmedia-api-production.json` (prod placeholder, `CHANGE_ME_*` values — same convention as `.env.production`). See `postman/README.md` for setup/flow details.

- **Auth is collection-level Bearer** bound to `{{jwt_token}}`, inherited by every request; only the endpoints `SecurityConfig` actually `permitAll()`s override to No Auth per-request. This was built by reading `SecurityConfig.java`'s `HttpMethod`-scoped rules directly, not inferred from URL shape — e.g. `GET /posts/{postId}/likes/check` and `GET /users/{userId}/follow/check` require auth (they check the *caller's* own status) even though sibling GETs on the same resource are public.
- **Request bodies are copied from the live DTOs**, not assumed — e.g. `UserUpdateRequest` has no `email` field, `LoginResponse` has `token`/`type`/`userId`/`username`/`email`/`expiresIn`. If a DTO's fields change, the collection's request bodies and test assertions (e.g. asserting `action` equals `"FOLLOWED"`/`"UNFOLLOWED"` in `FollowActionResponse`) will drift out of sync — update both together.
- Register/Login/Create Post/Create Comment have Tests scripts that auto-chain `jwt_token`/`user_id`/`post_id`/`comment_id` into the environment, so a fresh run only needs Register → Login before everything else works.
- `Delete Post`/`Delete Comment` are deliberately placed last in their folders since they invalidate `post_id`/`comment_id` for the requests above — don't reorder them into a blind top-to-bottom "run collection".
- If new endpoints are added or `Routes.java`/a DTO changes, update this collection by re-reading the controller and DTO source directly (as was done originally) rather than editing the JSON from memory — the whole point of this collection is that it matches what the code actually does, including the non-obvious auth/status-code cases above.

## Stack

Spring Boot 3.3.0, Java 21 (see JDK/Lombok note above for the multi-JDK setup), Maven. Key dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, JJWT 0.12.3 (JWT auth, using its post-0.12 non-deprecated API — `Jwts.parser()...parseSignedClaims()`, not the old `parserBuilder()`), Lombok, springdoc-openapi 2.0.2 (Swagger UI), Bean Validation. Test stack: `spring-boot-starter-test` + `spring-security-test`, MockMvc-based integration tests.

Base package for all application code: `com.shishir.socialmedia`.

## Git

This repo's local `user.name`/`user.email` are set to `softDevShishir` / `softdevshishir@gmail.com` (local config, not global — the machine's global git identity differs). Earlier commits before this was set show mixed author names (`NAZMUL SHISHIR`, `Shishir`); the two already pushed to `origin/main` were left as-is since rewriting them would require a force-push.
