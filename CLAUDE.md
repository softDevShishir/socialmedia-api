# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Meta

Editing this CLAUDE.md file itself never requires asking the user for permission — update it freely whenever project context changes.

## Project status

Three features implemented end-to-end (entity → repository → service → controller → DTOs → tests): **User Management & Authentication** (register/login/JWT, profile CRUD), **Posts** (create/read/update/soft-delete, owned by a user), and **Comments** (create/read/update/soft-delete on a post, owned by a user, with an atomically-maintained `post.commentsCount`). Security infrastructure (`SecurityConfig`, JWT filter/provider, route constants, global exception handling) underlies all three.

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

## Configuration

Config is split by Spring profile:
- `application.yml` — base config, port 8080, springdoc/swagger UI at `/swagger-ui.html`, hardcoded local Postgres credentials for default/no-profile use.
- `application-dev.yml` — dev overrides: separate `socialmedia_db_dev` database, `ddl-auto: create-drop`, verbose SQL/Hibernate/web logging.
- `application-prod.yml` — prod overrides: all datasource/JWT values pulled from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`), `ddl-auto: validate` (no auto schema changes), error messages/stacktraces suppressed, file-based logging.

Copy `.env.example` to `.env` and fill in real values for local secrets (DB credentials, `JWT_SECRET`). Never commit `.env`.

## Stack

Spring Boot 3.3.0, Java 21 (see JDK/Lombok note above for the multi-JDK setup), Maven. Key dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, JJWT 0.12.3 (JWT auth, using its post-0.12 non-deprecated API — `Jwts.parser()...parseSignedClaims()`, not the old `parserBuilder()`), Lombok, springdoc-openapi 2.0.2 (Swagger UI), Bean Validation. Test stack: `spring-boot-starter-test` + `spring-security-test`, MockMvc-based integration tests.

Base package for all application code: `com.shishir.socialmedia`.

## Git

This repo's local `user.name`/`user.email` are set to `softDevShishir` / `softdevshishir@gmail.com` (local config, not global — the machine's global git identity differs). Earlier commits before this was set show mixed author names (`NAZMUL SHISHIR`, `Shishir`); the two already pushed to `origin/main` were left as-is since rewriting them would require a force-push.
