# Architecture Decision Records (ADRs)

This document records the main architectural decisions of the **PetCare Hub Platform** backend, following the format proposed by Michael Nygard (*Context → Decision → Consequences*).

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-001](#adr-001-modular-monolith-instead-of-microservices) | Modular monolith instead of microservices | Accepted | 2026-10-24 |
| [ADR-002](#adr-002-layered-domain-driven-design-per-bounded-context) | Layered Domain-Driven Design per bounded context | Accepted | 2026-10-24 |
| [ADR-003](#adr-003-lightweight-cqrs-with-a-single-database) | Lightweight CQRS with a single database | Accepted | 2026-10-24 |
| [ADR-004](#adr-004-bounded-context-integration-through-acl-facades) | Bounded context integration through ACL facades | Accepted | 2026-10-24 |
| [ADR-005](#adr-005-separate-iam-credentials-from-profiles-data) | Separate IAM credentials from Profiles data | Accepted | 2026-11-04 |
| [ADR-006](#adr-006-stateless-authentication-with-jwt-and-bcrypt) | Stateless authentication with JWT and BCrypt | Accepted | 2026-10-27 |
| [ADR-007](#adr-007-mysql-as-the-relational-database) | MySQL as the relational database | Accepted | 2026-10-23 |
| [ADR-008](#adr-008-pluggable-file-storage-local-or-amazon-s3) | Pluggable file storage: local or Amazon S3 | Accepted | 2026-10-30 |
| [ADR-009](#adr-009-roboflow-for-computer-vision-with-one-model-per-image-type) | Roboflow for computer vision with one model per image type | Accepted | 2026-10-28 |
| [ADR-010](#adr-010-ai-supports-the-veterinarian-and-never-diagnoses) | AI supports the veterinarian and never diagnoses | Accepted | 2026-10-23 |
| [ADR-011](#adr-011-immutable-clinical-records) | Immutable clinical records | Accepted | 2026-10-24 |
| [ADR-012](#adr-012-docker-and-azure-container-apps-with-github-actions) | Docker and Azure Container Apps with GitHub Actions | Accepted | 2026-11-11 |
| [ADR-013](#adr-013-git-flow-conventional-commits-and-semantic-versioning) | Git Flow, Conventional Commits and Semantic Versioning | Accepted | 2026-10-23 |

---

## ADR-001: Modular monolith instead of microservices

**Status:** Accepted

### Context
PetCare Hub has seven bounded contexts (IAM, Profiles & Organization, Clinical Attention, Clinical Exams, Medical History, AI Assistance and Subscriptions). The team has five members, two sprints and a limited cloud budget. Microservices would require service discovery, distributed transactions, several databases and several deployments.

### Decision
Build a **modular monolith**: a single Spring Boot application and a single deployable artifact, where each bounded context is an isolated top-level package (`iam`, `profiles`, `clinicalattention`, `clinicalexams`, `aiassistance`, `medicalhistory`, `subscriptions`).

### Alternatives considered
- **Microservices:** independent scaling and deployment, but too much operational cost for the team size and timeline.
- **Traditional layered monolith:** simpler, but mixes domains and makes a future split very hard.

### Consequences
- ✅ One build, one deployment and one database: simple to run, test and deploy.
- ✅ Clear module boundaries allow extracting a context into a service later if needed.
- ⚠️ All contexts scale together.
- ⚠️ Boundaries are enforced by conventions and code review, not by the network.

---

## ADR-002: Layered Domain-Driven Design per bounded context

**Status:** Accepted

### Context
The clinical domain has rich rules (consultation states, exam validation, access by clinic). Business logic must be testable and must not depend on frameworks.

### Decision
Each bounded context follows the same four layers:

| Layer | Content |
|-------|---------|
| `domain` | Aggregates, entities, value objects, commands, queries, events and repository interfaces |
| `application` | Command and query services, event handlers and outbound ACL services |
| `infrastructure` | JPA repositories, storage, e-mail and external service adapters |
| `interfaces` | REST controllers, resources, assemblers and ACL facades |

### Alternatives considered
- **Package by layer** (all controllers together, all services together): mixes contexts.
- **Hexagonal architecture with separate Maven modules:** stronger isolation, but heavier for the course scope.

### Consequences
- ✅ Same structure in every context: easy onboarding for every team member.
- ✅ Business rules live in aggregates and are unit-testable.
- ⚠️ More classes (commands, queries, resources, assemblers) than a CRUD approach.

---

## ADR-003: Lightweight CQRS with a single database

**Status:** Accepted

### Context
Write operations change aggregates and must protect invariants; read operations return lists and details for dashboards. Mixing both in one service makes classes grow quickly.

### Decision
Separate **`CommandService`** (writes) from **`QueryService`** (reads) in every context, using **records** for commands and queries, over the **same MySQL database**. No event sourcing and no separate read model.

### Alternatives considered
- **Single service per aggregate:** fewer classes, but mixed responsibilities.
- **Full CQRS with a read database and event sourcing:** unnecessary complexity for the current load.

### Consequences
- ✅ Clear intent of every operation and smaller services.
- ✅ Reads can be optimized later without touching the domain.
- ⚠️ Reads and writes still share the same tables.

---

## ADR-004: Bounded context integration through ACL facades

**Status:** Accepted

### Context
Contexts need data from each other: Clinical Attention must check that a pet is a patient of the clinic (Profiles); AI Assistance needs the pet and the consultation. Accessing another context's repositories directly would couple their models.

### Decision
Each context exposes a **`ContextFacade`** in `interfaces/acl` with primitive types. Consumers call it through an **outbound service** in their own `application/internal/outboundservices/acl` that translates the result into their own model (Anti-Corruption Layer).

### Alternatives considered
- **Direct repository or entity access:** fast to write, but breaks encapsulation.
- **Asynchronous events only:** decoupled, but most checks must be synchronous (e.g. "is this pet a patient of the clinic?").

### Consequences
- ✅ A context can change its internal model without breaking others.
- ✅ Dependencies between contexts are explicit and easy to mock in tests.
- ⚠️ Extra mapping code at each boundary.

---

## ADR-005: Separate IAM credentials from Profiles data

**Status:** Accepted (agreed with the course professor)

### Context
Sign-up needs credentials (e-mail, password, account type) and personal data (names, phone, clinic, RUC). Storing everything in IAM would mix identity with business data.

### Decision
**IAM** stores only credentials, account type and roles. **Profiles** stores personal and organization data. Onboarding has two steps: sign-up and automatic sign-in in IAM, then "Complete your profile" in Profiles. Profiles validates the user against IAM through `IamContextFacade`.

### Alternatives considered
- **Single sign-up with all data in IAM:** fewer steps, but IAM becomes a business context.
- **Event-driven profile creation on `AccountRegistered`:** rejected because the profile needs data the user enters after sign-up.

### Consequences
- ✅ IAM is reusable and small.
- ✅ Each context owns its data.
- ⚠️ The frontend needs a guard that sends users without a profile to the completion screen.

---

## ADR-006: Stateless authentication with JWT and BCrypt

**Status:** Accepted

### Context
The API serves a web client now and a mobile client in Sprint 2. Server sessions would complicate scaling and the mobile client.

### Decision
Use **Spring Security** with **JWT** (jjwt) signed with `JWT_SECRET`, with the user's roles inside the token, and **BCrypt** for password hashing. Password recovery uses single-use tokens that expire in 30 minutes. The `prod` profile does not start without an explicit `JWT_SECRET`.

### Alternatives considered
- **Server sessions with cookies:** simple for web, poor fit for mobile and multiple instances.
- **External identity provider (Auth0, Azure AD B2C):** more features, extra cost and setup.

### Consequences
- ✅ Any instance can validate a request; web and mobile use the same mechanism.
- ⚠️ A token cannot be revoked before it expires (7 days by default).

---

## ADR-007: MySQL as the relational database

**Status:** Accepted (replaces PostgreSQL/Supabase from the initial deployment diagram)

### Context
The clinical data is relational (pets, clinics, consultations, exams) and needs transactions. The team already knows MySQL and the course uses it.

### Decision
Use **MySQL 8** in development and production (Azure Database for MySQL – Flexible Server), and **H2 in memory** for automated tests. Tables use a snake case and pluralized naming strategy.

### Alternatives considered
- **PostgreSQL (Supabase):** very capable, but less known by the team.
- **NoSQL (MongoDB):** flexible schema, weak fit for relational clinical data.

### Consequences
- ✅ Known technology and a managed service on Azure.
- ✅ Tests run without any external database.
- ⚠️ Small differences between H2 and MySQL must be watched.

---

## ADR-008: Pluggable file storage: local or Amazon S3

**Status:** Accepted

### Context
Clinical exams, profile photos and AI images produce files. Containers lose their disk on every restart, but developers need to work without cloud credentials.

### Decision
Define a **`FileStorageService`** port with two adapters, **local** and **S3**, selected by configuration (`clinical-exams.storage.provider`, `ai.storage.provider`). The database stores only the object key. Production uses S3 by default.

### Alternatives considered
- **Store files in the database (BLOB):** simple, but the database grows quickly.
- **Azure Blob Storage:** natural on Azure, but the S3 integration already existed in the team.

### Consequences
- ✅ Local development without AWS; production files survive restarts.
- ✅ Another provider can be added with a new adapter.
- ⚠️ AWS credentials must be managed as secrets.

---

## ADR-009: Roboflow for computer vision with one model per image type

**Status:** Accepted

### Context
Veterinarians analyze skin photos and X-rays. Available skin datasets include bounding boxes; X-ray datasets only have labels.

### Decision
Use **Roboflow** through an ACL adapter with **two models**: **object detection** for skin (`SKIN`, boxes and labels) and **classification** for X-rays (`RADIOGRAPH`, probabilities). The veterinarian chooses the image type; owners can only analyze skin photos. A **`fake` provider** is the default for development, demos and tests. Calls have an 8-second timeout.

### Alternatives considered
- **Own model with TensorFlow/PyTorch:** full control, but needs training infrastructure.
- **General vision APIs (Google Vision, Azure AI Vision):** not trained on veterinary lesions.

### Consequences
- ✅ Models trained on veterinary data with a free plan.
- ✅ The app works without Roboflow thanks to the fake provider.
- ⚠️ Dependency on an external service and its quota.

---

## ADR-010: AI supports the veterinarian and never diagnoses

**Status:** Accepted

### Context
Wrong AI output in a clinical setting can harm patients. Owners may misread technical results.

### Decision
AI results are **orientative**: they are linked to the pet and the consultation, the veterinarian always validates, edits or discards them, and the interface never labels them as a diagnosis. Owners only see results after a veterinarian validates them. If the AI fails, all clinical functions keep working.

### Alternatives considered
- **Automatic diagnosis:** faster, but clinically and ethically unacceptable.

### Consequences
- ✅ Responsibility stays with the veterinarian.
- ✅ Clinical work is never blocked by the AI.
- ⚠️ An extra validation step for the veterinarian.

---

## ADR-011: Immutable clinical records

**Status:** Accepted

### Context
A clinical history must be trustworthy and traceable. Editing a closed consultation would lose what was originally recorded.

### Decision
A consultation follows `CREATED → IN_PROGRESS → COMPLETED | CANCELLED`. Once **completed or cancelled it cannot be modified**. Corrections are added as **re-evaluations** of a diagnosis, keeping the original record and the responsible veterinarian.

### Alternatives considered
- **Free editing with an audit log:** flexible, but the main record could still be overwritten.

### Consequences
- ✅ Full traceability, aligned with good clinical practice.
- ⚠️ Mistakes are fixed by adding records, not by editing.

---

## ADR-012: Docker and Azure Container Apps with GitHub Actions

**Status:** Accepted

### Context
The API must run the same way in every environment and be deployed repeatably, without manual steps.

### Decision
Package the API with a **multi-stage Dockerfile** (Maven build, Eclipse Temurin 25 JRE, non-root user). Deploy to **Azure Container Apps** with **Azure Database for MySQL – Flexible Server** and images in **Azure Container Registry**. **GitHub Actions** runs CI (tests and image build) on every pull request and CD on every release tag (`v*.*.*`).

### Alternatives considered
- **Azure App Service with a JAR:** simpler, but tied to the platform runtime.
- **Render or Railway:** easy, but outside the Azure student credits.
- **Kubernetes (AKS):** powerful, but too complex for the project.

### Consequences
- ✅ Same image in local, CI and production; HTTPS and scale-to-zero included.
- ✅ No release reaches production without passing the tests.
- ⚠️ Depends on an active Azure subscription and its credits.

---

## ADR-013: Git Flow, Conventional Commits and Semantic Versioning

**Status:** Accepted

### Context
Five developers work in parallel on different contexts and deliver at the end of each sprint.

### Decision
Use **Git Flow** (`main`, `develop`, `feature/*`, `release/*`, `hotfix/*`), **Conventional Commits** in English and **Semantic Versioning**, with a [CHANGELOG](../CHANGELOG.md) in *Keep a Changelog* format. Every sprint ends with a tagged release (`v1.0.0` for Sprint 1).

### Alternatives considered
- **Trunk-based development:** faster integration, but needs more automation and discipline.
- **GitHub Flow:** simpler, but without explicit release branches.

### Consequences
- ✅ Readable history and clear versions per sprint.
- ⚠️ More branches and merges to manage.
