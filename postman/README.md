# Postman Collection for Social Media API

Covers all seven implemented features end-to-end: **Authentication**, **Users**, **Posts**, **Comments**, **Likes**, **Follow**, and **Feed & Timeline**, plus **Search** — 31 endpoints in 8 folders, matching the live `com.shishir.socialmedia` controllers and `SecurityConfig`.

## Files

| File | Purpose |
|---|---|
| `socialmedia-api.postman_collection.json` | The collection: all requests, folders, pre-request/test scripts, collection-level Bearer auth. |
| `socialmedia-api-environment.json` | Local development environment (`http://localhost:8080`). |
| `socialmedia-api-production.json` | Production environment template — `base_url` and secrets are `CHANGE_ME_*` / blank placeholders, not real values. |

## Setup

### 1. Import the collection
Postman → **Import** → select `socialmedia-api.postman_collection.json`.

### 2. Import an environment
Postman → **Environments** → **Import** → select `socialmedia-api-environment.json` (or the production file). Select it from the environment dropdown in the top-right — every request uses `{{base_url}}`, so nothing else needs editing to switch dev/prod.

### 3. Run the app
```
mvn spring-boot:run
```
Defaults to the `dev` profile, port 8080 (see the project's root `CLAUDE.md` for details on profiles and the local Postgres setup required for the app to start).

## Auth model

The collection uses **collection-level Bearer auth** bound to the `{{jwt_token}}` variable — every request inherits it automatically, so you never paste a token into individual requests. Endpoints that are `permitAll()` in `SecurityConfig` (public GETs, register, login) explicitly override this to **No Auth** at the request level, matching the real access rules exactly (see the Endpoint Overview table below — this is not a guess, it was read directly from `SecurityConfig.java`).

## Quick start (Authentication folder, in order)

1. **Register User** — creates an account from the `test_email` / `test_password` / `test_username` variables. On success, its Tests script saves `user_id` and `username`.
2. **Login User** — authenticates and saves `jwt_token`, `user_id`, `username` automatically. Every subsequent authenticated request picks up `jwt_token` via the collection's inherited Bearer auth — no manual copy-paste.
3. **Get Current User** — confirms the token works.

Registration fails with `409` if you rerun it with the same email/username — either change `test_email`/`test_username` in the environment, or just rerun Login (the account already exists).

## Typical end-to-end flow

1. **Authentication** → Register User → Login User
2. **Posts** → Create Post (saves `post_id`)
3. **Comments** → Create Comment on that post (saves `comment_id`)
4. **Likes** → Like Post → Check if User Liked Post → Unlike Post
5. **Follow** → set `target_user_id` to a second account's ID (register a second user first, or use an ID from **Users → Get All Users**), then Follow User → Check if Following → Unfollow User
6. **Feed & Timeline** → Get User Feed (needs auth) / Get User Timeline / Get Explore Feed (both public)
7. **Search** → Search Users / Search Posts

`Delete Post` and `Delete Comment` are placed at the end of their folders since they invalidate `post_id`/`comment_id` for the requests above them — run them deliberately, not as part of an unattended top-to-bottom collection run.

## Variables

| Variable | Set by | Notes |
|---|---|---|
| `base_url` | Environment | Server URL; switch environments to change dev/prod. |
| `jwt_token` | Login User (auto) | Backs the collection-level Bearer auth. |
| `user_id` | Register/Login/Get User by Username (auto) | The logged-in caller's own ID. |
| `target_user_id` | Manual | A *different* user's ID, for Follow/Unfollow (you can't follow yourself). |
| `post_id` | Create Post (auto) | Used by Comments, Likes, Update/Delete Post. |
| `comment_id` | Create Comment (auto) | Used by Update/Delete Comment. |
| `username` | Manual / Get User by Username | For profile lookup/update. |
| `test_email`, `test_password`, `test_username` | Manual | Credentials used by Register/Login. |

## Endpoint overview

Auth column reflects `SecurityConfig`'s actual `HttpMethod`-scoped `permitAll()` rules, not assumptions from the URL shape.

| Feature | Endpoints | Public | Auth Required |
|---|---|---|---|
| Authentication | 3 | 2 (register, login) | 1 (me) |
| Users | 3 | 2 (list, get by username) | 1 (update) |
| Posts | 6 | 4 (list, get by id, get user posts, +) | 2 (create/update; delete) — see note |
| Comments | 5 | 2 (list, get by id) | 3 (create, update, delete) |
| Likes | 4 | 1 (get post likes) | 3 (like, unlike, check) |
| Follow | 5 | 2 (followers, following) | 3 (follow, unfollow, check) |
| Feed & Timeline | 3 | 2 (timeline, explore — work with or without a token) | 1 (feed) |
| Search | 2 | 2 | 0 |
| **Total** | **31** | **17** | **14** |

Note: Posts is 4 public GETs (list, get-by-id, get-user-posts) + 3 auth-required writes (create, update, delete) = 7 rows of behavior across 6 endpoints, since create/update/delete share the auth-required bucket.

## Response codes actually thrown by the service layer

Documented per-endpoint in each request's description; summarized here:

- `200` — success (GET, PUT, and `Unfollow User` specifically — it returns `200` with a body, not `204`)
- `201` — created (register, create post/comment, like, follow)
- `204` — no content (delete post/comment, unlike)
- `400` — validation failed, invalid pagination, or acting on a soft-deleted resource
- `401` — missing/invalid JWT (authentication failure)
- `403` — `AccessDeniedException`: authenticated, but not the owner (e.g. updating someone else's post/profile) — distinct from 401
- `404` — resource not found, or (for check endpoints) the relationship doesn't exist
- `409` — conflict: duplicate email/username, already liked, already following

## Tips

- `{{base_url}}/swagger-ui.html` has the live OpenAPI docs if you want to cross-check a request shape.
- The collection-level Tests script checks response time (<5s) and JSON content-type on every request — failures there usually mean the server errored with an HTML/plain-text body instead of the expected JSON.
- If `SELECT * FROM users` in Postgres looks unexpected after running `mvn test`, that's the integration test suite's seed data, not this collection — see the Testing section of the project's root `CLAUDE.md`.
- Pagination defaults to `page=0&pageSize=10` in Feed/Timeline/Explore (max 100) and Search (max 50); tune the query params per request as needed.
