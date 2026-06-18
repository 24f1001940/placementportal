# Placement Portal Application

Full-stack Java 25 learning project based on the provided PDF. It includes a Spring Boot backend, React + Vite frontend, JWT role-based auth, resume upload, Redis caching, RabbitMQ notification fallback, Docker Compose, tests, Swagger, and GitHub Actions.

## Modules

- Admin: approve companies, approve/reject drives, view statistics, search users, blacklist users, export applications CSV.
- Company: create profile, create placement drives, view applicants, shortlist/update application status, download resumes.
- Student: update profile, upload/download resume, browse drives, apply once per drive, view application history.

## Local Setup

1. Start infrastructure:

```powershell
cd c:\placementportal
docker compose up -d mysql redis rabbitmq
```

2. Run backend:

```powershell
cd c:\placementportal\backend
.\mvnw.cmd spring-boot:run
```

If Docker is not running or MySQL is not configured yet, run the backend with the in-memory dev profile:

```powershell
cd c:\placementportal\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

3. Run frontend:

```powershell
cd c:\placementportal\frontend
npm install
npm run dev
```

4. Open:

- Frontend: http://localhost:5173
- API: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html
- RabbitMQ UI: http://localhost:15672

## Demo Users

- Admin: `admin@placement.local` / `Admin@123`
- Company: `hr@novacore.local` / `Company@123`
- Student: `student@placement.local` / `Student@123`

## Docker Setup

```powershell
cd c:\placementportal
docker compose up --build
```

Then open http://localhost:5173.

## Verification

```powershell
cd c:\placementportal\backend
.\mvnw.cmd test

cd c:\placementportal\frontend
npm run lint
npm run build
```

## Backend Structure

- `controller`: REST endpoints
- `service`: business logic
- `repository`: Spring Data JPA repositories
- `entity`: database models
- `dto`: request/response records
- `security`: JWT filter, auth, security config
- `config`: RabbitMQ/Jackson config
- `exception`: global API error handling

## Frontend Structure

- `pages`: Login, Register, dashboards, profile, history, drive details
- `components`: shared UI widgets
- `layouts`: authenticated app shell
- `context`: auth state
- `services`: Axios API client
- `utils`: formatting helpers
