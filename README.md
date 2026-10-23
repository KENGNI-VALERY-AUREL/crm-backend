# crm-backend

Spring Boot backend for the CRM mobile app (prospect/client follow-up digitalization,
Cameroon market). See the cahier des charges (French, functional/business document) for
the full specification — section 7.3 for the data model, section 12 for the architecture.

## Current step

Step 7: group & user management — UC-301 (SUPER_ADMIN structures the company), UC-201
(HOST manages the members of their group), UC-202 (reassign a prospect).
`V4__users_email_unique_active.sql` makes email uniqueness apply to active users only.

Done so far: skeleton + identity (1-2), JWT auth + tenant context (3), prospects &
actions (4-5), pipeline (6), groups & users (7).

## ⚠️ First thing to do with this version: initialize Git

This project has been regenerated from scratch a few times, which forced you to redo
local fixes each time. To stop that:

```bash
git init
git add .
git commit -m "chore: project skeleton + identity module (step 1-2)"
```

Then create an empty `crm-backend` repository on GitHub and push:

```bash
git remote add origin <your-repo-url>
git branch -M main
git push -u origin main
```

From now on, future changes will be delivered as small, targeted diffs to apply on top
of this repository — not as a full project archive each time.

## Prerequisites

- JDK 17
- Maven (or the `mvnw` wrapper)
- Docker Desktop (for local PostgreSQL)
- VS Code with the recommended extensions (`.vscode/extensions.json`)

## Running locally

1. Copy the environment file:
   ```bash
   cp .env.example .env
   ```

2. Start PostgreSQL locally:
   ```bash
   docker compose up -d
   docker compose ps   # the "postgres" service should be "healthy"
   ```

3. Run the application:
   - **Via VS Code**: *Run and Debug* tab → `CrmBackendApplication` configuration.
   - **Via the terminal**:
     ```bash
     mvn spring-boot:run
     ```

   On startup, Flyway automatically applies `V1__identity_schema.sql` to the local
   database, then Hibernate validates that the JPA entities exactly match the created
   schema (`ddl-auto: validate`). If everything matches, the application starts normally.

4. Check that it's running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Should return `{"status":"UP"}`.

5. (Optional) Inspect the created tables:
   ```bash
   docker exec -it crm-postgres psql -U crm_user -d crm_db -c "\dt"
   ```
   Should list: `organizations`, `groups`, `users`, `supervisions`, and
   `flyway_schema_history` (Flyway's own migration-tracking table).

## Testing the API with Swagger

After starting the application, open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).
The OpenAPI specification is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

Use `POST /auth/register` or `POST /auth/login` to get a JWT in the response. Select
**Authorize** in Swagger UI and enter the token value; Swagger UI will send it as a
`Bearer` token when calling endpoints that require authentication.

## Package structure

Each module is organized by layer, not as one flat folder:

```
identity/
  model/        entities (Organization, Group, User, Supervision, SupervisionId)
  enums/        UserRole
  repository/   Spring Data JPA repositories
  service/      (added in step 3)
  controller/   (added in step 3)
```

| Package | Responsibility | Current content |
|---|---|---|
| `identity` | Authentication, organizations, groups, roles (RBAC) | Entities + repositories (step 2) |
| `prospects` | Prospect records and follow-up actions | Implemented (steps 4-5) |
| `pipeline` | Sales pipelines, stages, stage-change history, Kanban | Implemented (step 6) |
| `communication` | Message templates, SMS/WhatsApp/Email workflows | Not yet implemented |
| `billing` | Packs, subscriptions, transactions, Mobile Money | Not yet implemented |
| `reporting` | Dashboards per role | Not yet implemented |
| `common` | Multi-tenant security, files, audit, errors — cross-cutting | `BaseEntity`, `JpaAuditingConfig` |

## Identity module — data model

- `Organization` — a company (or a solo user's personal space).
- `Group` — a team, attached to an organization.
- `User` — an account, attached to a group (primary membership), with a `role`
  (`SUPER_ADMIN`, `HOST`, `MEMBER`).
- `Supervision` — join entity between `User` (host) and `Group` (many-to-many), because
  a host can supervise several groups (cahier des charges, arbitrage 8.1).
- `Organization`, `Group`, and `User` all extend `BaseEntity` (`common.model` package):
  logical deletion via `isDeleted` / `deletedDate`, never physical deletion (arbitrage
  8.8). `createdDate` / `updatedDate` are filled in automatically by Spring Data JPA
  Auditing (`JpaAuditingConfig`) — no manual code needed in each entity.

## Pipeline endpoints (step 6)

| Method | Path | Role |
|---|---|---|
| GET | `/pipelines` | any |
| GET | `/pipelines/{id}/board` | any (MEMBER sees only own prospects) |
| POST | `/pipelines/{id}/stages` | HOST / SUPER_ADMIN |
| PATCH | `/pipelines/{id}/stages/{stageId}` | HOST / SUPER_ADMIN |
| DELETE | `/pipelines/{id}/stages/{stageId}` | HOST / SUPER_ADMIN (409 if the stage has prospects) |
| PATCH | `/prospects/{id}/stage` | any with access to the prospect (`reason` required for LOST stages) |
| GET | `/prospects/{id}/stage-history` | any with access to the prospect |

## Group & user endpoints (step 7)

| Method | Path | Role |
|---|---|---|
| GET | `/groups` | HOST (own group) / SUPER_ADMIN (all) |
| POST | `/groups` | SUPER_ADMIN |
| PATCH | `/groups/{id}` (rename) | SUPER_ADMIN |
| GET | `/users?groupId=` | HOST (own group) / SUPER_ADMIN (all) |
| POST | `/users` | SUPER_ADMIN (HOST or MEMBER, any group) / HOST (MEMBER, own group) |
| PATCH | `/users/{id}/role` | SUPER_ADMIN |
| DELETE | `/users/{id}` | SUPER_ADMIN / HOST (members only); 409 if the user still owns prospects |
| PATCH | `/prospects/{id}/assignee` | HOST / SUPER_ADMIN; target must be in the prospect's group |

V1 limits (cahier des charges, section 8.10): a HOST manages only their own group;
supervising several groups (`supervisions` table) is V2.

## Known gap

The JWT carries `role` and `groupId` and is not re-checked against the database: a removed
user, or one whose role changed, keeps the old rights until the token expires
(`JWT_EXPIRATION_MS`, 24 h by default).

## Next step

Billing module: packs, subscriptions, quota counter, Mobile Money webhooks (Module 6).
