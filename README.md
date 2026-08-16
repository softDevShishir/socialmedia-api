# Social Media REST API

## 🚀 Live API

**API Base URL:** https://socialmedia-api-gsvh.onrender.com
**Swagger UI:** https://socialmedia-api-gsvh.onrender.com/swagger-ui.html

Status: ✅ Live on Render

**More docs:** [DEPLOYMENT.md](DEPLOYMENT.md) (local / Docker / Render deploy steps) · [TESTING.md](TESTING.md) (curl / Swagger / Postman / `mvn test`) · [QUICK_START.md](QUICK_START.md) (full curl walkthrough) · [postman/README.md](postman/README.md)

---

## Quick Start (Production)

### Register User
```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123",
    "username": "testuser",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

### Create Post (Authenticated)
```bash
curl -X POST https://socialmedia-api-gsvh.onrender.com/api/v1/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello from Social Media API!"
  }'
```

### Get All Posts
```bash
curl https://socialmedia-api-gsvh.onrender.com/api/v1/posts
```

### Search Users
```bash
curl "https://socialmedia-api-gsvh.onrender.com/api/v1/search/users?query=test"
```

---

## Features (7 Complete)

✅ User Management & Authentication
✅ Posts (Create, Read, Update, Delete)
✅ Comments
✅ Likes
✅ Follow System
✅ Feed/Timeline
✅ Search

---

## API Documentation

- **Swagger UI:** https://socialmedia-api-gsvh.onrender.com/swagger-ui.html
- **Postman Collection:** Import `postman/socialmedia-api.postman_collection.json`
- **Full Endpoints:** 31+ endpoints covering all 7 features

---

## Technology Stack

- **Java:** 21
- **Framework:** Spring Boot 3.3.0
- **Database:** PostgreSQL 16
- **Authentication:** JWT (jjwt 0.12.3)
- **API Docs:** Swagger/OpenAPI 3.0
- **Build:** Maven
- **Deployment:** Render.com

---

## Local Development

```bash
git clone https://github.com/softDevShishir/socialmedia-api.git
cd socialmedia-api
```

Full setup (database creation, seeding, run command) for all three ways to run this locally — plain local, Docker, and how the Render production deploy works — is in **[DEPLOYMENT.md](DEPLOYMENT.md)**. Short version: create the Postgres role/databases, then `mvn spring-boot:run` (dev profile, port 8080) or `docker compose up -d --build` (prod profile against a containerized Postgres).

## Testing

See **[TESTING.md](TESTING.md)** for all four ways to exercise the API: automated (`mvn test`), curl, Swagger UI, and the Postman collection.

---

## Endpoints Summary

| Feature | Endpoints | Status |
|---------|-----------|--------|
| Authentication | 3 | ✅ Live |
| Users | 3 | ✅ Live |
| Posts | 6 | ✅ Live |
| Comments | 5 | ✅ Live |
| Likes | 4 | ✅ Live |
| Follow | 5 | ✅ Live |
| Feed | 3 | ✅ Live |
| Search | 2 | ✅ Live |
| **Total** | **31+** | ✅ **Live** |

---

## Author

**Shishir** - Backend Engineer
- GitHub: github.com/softDevShishir
- Email: softdevshishir@gmail.com
