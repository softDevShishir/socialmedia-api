# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Meta

Editing this CLAUDE.md file itself never requires asking the user for permission — update it freely whenever project context changes.

## Project status

Scaffold plus a security layer: `SocialMediaApiApplication`, JWT auth (`security/JwtTokenProvider`, `security/JwtAuthenticationFilter`), `config/SecurityConfig`, route constants (`config/Routes`), an audit base entity (`config/AuditData`), and a full exception-handling stack under `exception/` (custom exceptions, `ExceptionResponse` DTO, `GlobalExceptionHandler`). No controllers, services, entities, or repositories exist yet.

## JDK 25 / Lombok

The local JDK is 25. Lombok stopped being auto-discovered as an annotation processor on JDK 25's javac (processors on the plain `-classpath` are silently ignored; only `-processorpath`/`annotationProcessorPaths` is honored). `pom.xml` pins `lombok.version` to `1.18.44` (Spring Boot 3.3.0's parent BOM defaults to 1.18.32, too old for JDK 25) and explicitly declares `annotationProcessorPaths` for lombok in the `maven-compiler-plugin` config. If Lombok-generated members (`log`, generated getters/setters/builders) start failing to compile with "cannot find symbol", check these two things first before assuming the source is wrong.

## Commands

- Build: `mvn clean install`
- Run locally: `mvn spring-boot:run` (defaults to the `dev` profile per `application.yml` / `SPRING_PROFILES_ACTIVE`)
- Run tests: `mvn test`
- Run a single test class: `mvn test -Dtest=ClassName`
- Run a single test method: `mvn test -Dtest=ClassName#methodName`
- Package: `mvn clean package`

## Configuration

Config is split by Spring profile:
- `application.yml` — base config, port 8080, springdoc/swagger UI at `/swagger-ui.html`, hardcoded local Postgres credentials for default/no-profile use.
- `application-dev.yml` — dev overrides: separate `socialmedia_db_dev` database, `ddl-auto: create-drop`, verbose SQL/Hibernate/web logging.
- `application-prod.yml` — prod overrides: all datasource/JWT values pulled from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`), `ddl-auto: validate` (no auto schema changes), error messages/stacktraces suppressed, file-based logging.

Copy `.env.example` to `.env` and fill in real values for local secrets (DB credentials, `JWT_SECRET`). Never commit `.env`.

## Stack

Spring Boot 3.3.0, Java 21, Maven. Key dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, JJWT 0.12.3 (JWT auth), Lombok, springdoc-openapi 2.0.2 (Swagger UI), Bean Validation. Test stack: `spring-boot-starter-test` + `spring-security-test`.

Base package for all application code: `com.shishir.socialmedia`.
