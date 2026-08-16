# Quick Start - Social Media API

## Live API: https://socialmedia-api-gsvh.onrender.com

Every command below also works against a local/Docker instance — just swap the base URL for `http://localhost:8080`. See [TESTING.md](TESTING.md) for other ways to test (Swagger UI, Postman, automated tests) and [DEPLOYMENT.md](DEPLOYMENT.md) for how to run the API locally or via Docker.

### 1️⃣ Register User

```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "shishir@example.com",
    "password": "MySecurePass123!",
    "username": "shishir_test",
    "firstName": "Shishir",
    "lastName": "Dev"
  }'
```

**Response:**
```json
{
  "id": 1,
  "email": "shishir@example.com",
  "username": "shishir_test",
  "createdAt": "2026-08-11T10:00:00"
}
```

### 2️⃣ Login

```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "shishir@example.com",
    "password": "MySecurePass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "shishir_test",
  "email": "shishir@example.com",
  "expiresIn": 86400
}
```

**Save the token!** You'll need it for authenticated requests.

### 3️⃣ Create Post

```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/posts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "My first post on Social Media API! 🚀"
  }'
```

### 4️⃣ Get All Posts

```bash
curl https://socialmedia-api-gsvh.onrender.com/api/v1/posts
```

### 5️⃣ Like a Post

```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/posts/1/likes \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 6️⃣ Follow User

```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/users/2/follow \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 7️⃣ Get Your Feed

```bash
curl https://socialmedia-api-gsvh.onrender.com/api/v1/feed \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 8️⃣ Search Users

```bash
curl "https://socialmedia-api-gsvh.onrender.com/api/v1/search/users?query=shishir"
```

## Use Swagger UI

Visit: https://socialmedia-api-gsvh.onrender.com/swagger-ui.html

Test all endpoints interactively with Swagger!
