# PetCare Hub Platform

PetCare Hub Platform is a RESTful API built on the foundations of Domain-Driven Design (DDD). Developed with Java 25, Spring Boot 4.1.2, and MySQL, the platform supports veterinary clinics and pet owners with a longitudinal, pet-centered clinical record: consultations, clinical exams, and AI-assisted image analysis. It features a modular monolith organized into bounded contexts and applies Command Query Responsibility Segregation (CQRS) principles, keeping a clean separation between data modification and retrieval operations.

---

## Tech Stack

- **Language:** Java 25 (LTS)
- **Framework:** Spring Boot 4.1.2 (Web MVC, Data JPA, Security, Validation, Mail)
- **Database:** MySQL 8+ (H2 in-memory for tests)
- **Persistence:** Spring Data JPA / Hibernate with a Snake Case and Pluralized Table Physical Naming Strategy
- **Security:** Spring Security with JWT (jjwt 0.13) & BCrypt
- **File Storage:** Local disk or Amazon S3 (AWS SDK 2)
- **Computer Vision:** Roboflow (skin lesion detection and X-ray classification)
- **Documentation:** SpringDoc OpenAPI 3.2 (Swagger UI)
- **Build Tool:** Maven 3.9+ (wrapper included)
- **Utilities:** Lombok
- **Containerization:** Docker (multi-stage image on Eclipse Temurin 25)
- **Cloud:** Microsoft Azure (Container Apps, Azure Database for MySQL)

---

## Prerequisites

- Java 25 JDK
- Maven 3.9+ (or the included `mvnw` wrapper)
- MySQL 8.0+
- Docker (optional)

---

## Project Structure

The project is organized into bounded contexts following Layered Domain-Driven Design (DDD):

```text
src/main/java/com/nexoshift/petcarehub/
├── iam/                # Identity and Access Management (Accounts, Users, Roles, JWT, BCrypt, password recovery)
├── profiles/           # Profiles & Organization (Pet Owners, Pets, Clinics, Veterinarians, Patients, Consulting Rooms)
├── clinicalattention/  # Clinical Attention (Consultations, Anamnesis, Assessments, Findings, Diagnoses, Re-evaluations)
├── clinicalexams/      # Clinical Exams (Requests, Results, Validation, Files, Clinic worklist)
├── aiassistance/       # AI Assistance (Computer vision for skin photos and X-rays)
├── medicalhistory/     # Medical History (planned for Sprint 2)
├── subscriptions/      # Payment & Subscriptions (planned for Sprint 2)
└── shared/             # Shared kernel (Auditable aggregate base, domain exceptions, error handling, naming strategy, storage, OpenAPI)
```

Each bounded context follows a classic 4-layer structure:
- `domain`: Aggregates, entities, value objects, commands, queries, domain events, and repository interfaces.
- `application`: Command services, query services, internal implementations, event handlers, and outbound ACL services.
- `infrastructure`: JPA repositories, storage adapters, security configuration, and external service adapters (S3, Roboflow, SMTP).
- `interfaces`: REST controllers, resources, assemblers, and ACL facades.

---

## Configuration

The project uses profile-based configuration located in `src/main/resources`:
- `application.properties`: Shared defaults, database naming strategy, JWT, CORS, storage, and AI settings.
- `application-dev.properties`: Development settings (default profile).
- `application-prod.properties`: Production settings, fully driven by environment variables.

### Environment Variables

Required environment variables (especially for the `prod` profile):

| Variable                                       | Description                                   | Default (Dev)                                    |
|------------------------------------------------|-----------------------------------------------|--------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                       | Active Spring profile                         | `dev`                                            |
| `PORT`                                         | Application port                              | `8080`                                           |
| `DB_URL`                                       | MySQL JDBC URL                                | `jdbc:mysql://localhost:3306/petcarehub`         |
| `DB_USERNAME`                                  | Database username                             | `root`                                           |
| `DB_PASSWORD`                                  | Database password                             | `root`                                           |
| `JWT_SECRET`                                   | Secret key for JWT signing (≥ 32 bytes)       | development value; **required in `prod`**        |
| `JWT_EXPIRATION_DAYS`                          | Token lifetime in days                        | `7`                                              |
| `CORS_ALLOWED_ORIGINS`                         | Allowed web origins                           | `http://localhost:4200`                          |
| `PASSWORD_RESET_URL`                           | Link sent in the password recovery e-mail     | `http://localhost:4200/reset-password`           |
| `PASSWORD_RESET_TOKEN_MINUTES`                 | Recovery token validity                       | `30`                                             |
| `MAIL_USERNAME`, `MAIL_PASSWORD`               | SMTP account (Gmail)                          | —                                                |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`   | Amazon S3 credentials                         | —                                                |
| `AWS_S3_BUCKET`, `AWS_REGION`                  | Amazon S3 bucket and region                   | `petcarehub-storage`, `us-east-2`                |
| `FILE_STORAGE_LOCAL_DIRECTORY`                 | Folder for exam files stored locally          | `uploads`                                        |
| `AI_VISION_PROVIDER`                           | Computer vision provider: `fake` or `roboflow`| `fake`                                           |
| `ROBOFLOW_API_KEY`                             | Roboflow API key                              | —                                                |
| `ROBOFLOW_SKIN_MODEL_ID`, `ROBOFLOW_RADIOGRAPH_MODEL_ID` | Roboflow model identifiers          | —                                                |
| `AI_STORAGE_PROVIDER`                          | Storage for AI images: `local` or `s3`        | `local` (`s3` in prod)                           |
| `CLINICAL_EXAMS_STORAGE_PROVIDER`              | Storage for exam files: `local` or `s3`       | `local` (`s3` in prod)                           |

> Never commit real secrets. Use environment variables or your IDE run configuration.

---

## Getting Started

### 1) Database Setup
Ensure MySQL is running. With the `dev` profile the `petcarehub` database is created automatically (`createDatabaseIfNotExist=true`).

### 2) Run the Application

#### Using Maven
```bash
# Run with default 'dev' profile
./mvnw clean spring-boot:run

# Run with specific profile and environment variables
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:mysql://localhost:3306/petcarehub \
DB_USERNAME=root \
DB_PASSWORD=password \
JWT_SECRET=your-secret-with-at-least-32-bytes \
PORT=8080 \
./mvnw clean spring-boot:run
```

On Windows use `mvnw.cmd` and set the variables in the IntelliJ IDEA run configuration.

#### Using Docker
```bash
# Build the image
docker build -t petcarehub-api:1.0.0 .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/petcarehub?serverTimezone=UTC" \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your-secret-with-at-least-32-bytes \
  -e AWS_ACCESS_KEY_ID=your-key \
  -e AWS_SECRET_ACCESS_KEY=your-secret \
  -e AWS_S3_BUCKET=your-bucket \
  -e AWS_REGION=your-region \
  petcarehub-api:1.0.0
```

The image runs with the `prod` profile as a non-root user and stores files on Amazon S3. In production it is deployed on **Azure Container Apps** with **Azure Database for MySQL – Flexible Server**.

### 3) API Documentation
Once running, access the interactive documentation at:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Scripts and Commands

- `./mvnw clean compile`: Compile the project.
- `./mvnw test`: Run unit, integration, and REST tests (302 tests on an in-memory H2 database, no MySQL, S3, or Roboflow needed).
- `./mvnw package`: Package the application into a JAR file.
- `./mvnw spring-boot:run`: Launch the application.
- `docker build -t petcarehub-api:1.0.0 .`: Build the Docker image.

On Windows PowerShell use `.\mvnw` instead of `./mvnw`.

---

## CI/CD

The project uses **GitHub Actions** (`.github/workflows/`):

| Workflow | Trigger | Steps |
|----------|---------|-------|
| `ci.yml` | Pull requests and pushes to `develop` and `main` | Build and run all tests on Java 25, then build the Docker image |
| `cd.yml` | Release tags (`v*.*.*`) or manual run | Run tests, build the image in Azure Container Registry and update the Azure Container App |

Deployment secrets (`AZURE_CREDENTIALS`) and variables (`AZURE_RESOURCE_GROUP`, `AZURE_ACR_NAME`, `AZURE_CONTAINER_APP`) are configured in the repository settings. Application secrets (database, JWT, S3, mail) live only in Azure.

---

## API Surface

All endpoints live under `/api/v1` and, except authentication, require `Authorization: Bearer <token>`.

### IAM (Identity and Access Management)
- `POST /api/v1/authentication/sign-up`: Register a pet owner or clinic account.
- `POST /api/v1/authentication/sign-in`: Authenticate and obtain a JWT token.
- `POST /api/v1/authentication/password-recovery` · `POST /api/v1/authentication/password-reset`: Password recovery by e-mail.
- `GET /api/v1/users/me`: Authenticated user.
- `/api/v1/users/{userId}`: Password change, activation, and deactivation.
- `POST /api/v1/accounts/{accountId}/users`: Add a veterinarian to a clinic account.
- `GET /api/v1/roles`: Available roles.

### Profiles & Organization
- `/api/v1/pet-owners`: Pet owner profiles and profile image.
- `/api/v1/pet-owners/{petOwnerId}/pets` · `/api/v1/pets`: Pets (search by `petCode`).
- `/api/v1/pets/{petId}/clinics`: Clinics where a pet is a patient.
- `/api/v1/clinics`: Clinic profiles.
- `/api/v1/clinics/{clinicId}/patients`: Clinic patients.
- `/api/v1/clinics/{clinicId}/veterinarians` · `/api/v1/veterinarians`: Veterinarians.
- `/api/v1/clinics/{clinicId}/consulting-rooms`: Consulting rooms (activate, deactivate).
- `/api/v1/clinics/{clinicId}/consulting-room-assignments`: Assignment of veterinarians to consulting rooms.

### Clinical Attention
- `/api/v1/clinical-consultations`: Consultation lifecycle (`start`, `complete`, `cancel`).
- `/api/v1/clinical-consultations/{consultationId}/anamnesis`, `/physiological-assessments`, `/findings`, `/diagnoses`, `/diagnoses/{diagnosisId}/reevaluations`: Clinical records of a consultation.
- `GET /api/v1/pets/{petId}/clinical-consultations`: Consultations of a pet.

### Clinical Exams
- `/api/v1/clinical-exams`: Exam requests and clinic worklist (`start`, `result`, `result/validate`, `complete`, `cancel`).
- `/api/v1/clinical-exams/{examId}/files`: Exam files (image, video, PDF up to 10 MB).
- `GET /api/v1/clinical-consultations/{consultationId}/clinical-exams` · `GET /api/v1/pets/{petId}/clinical-exams`: Exams of a consultation or a pet.

### AI Assistance
- `/api/v1/ai/vision-analyses`: Computer vision analysis of skin photos and X-rays (by `petId` or `consultationId`).

---

## Architecture & Development Conventions

- **Domain-Driven Design (DDD) & CQRS:** Write operations use `CommandService`, read operations use `QueryService`.
- **Aggregate Encapsulation:** Aggregates protect their invariants through domain business methods; state transitions (for example, a consultation `CREATED → IN_PROGRESS → COMPLETED`) are validated inside the aggregate.
- **Bounded Context Integration:** Contexts communicate only through ACL facades (`ContextFacade`) and outbound services, never through another context's repositories.
- **Domain Events:** Aggregates extend `AuditableAbstractAggregateRoot` and register events (`AccountRegistered`, `ClinicalConsultationCompleted`) that are handled by application event handlers.
- **Immutable Clinical Records:** Completed or cancelled consultations cannot be modified; corrections are added as re-evaluations.
- **Access Rules:** A veterinarian only works with patients of their own clinic; a pet owner only sees their own pets and **validated** exam results.
- **Centralized Error Handling:** A global `@RestControllerAdvice` maps domain exceptions to `400`, `401`, `403`, `404`, `409`, `413`, and `422` with a common `ErrorResource` (status, error, message, path, timestamp, details).
- **Pluggable Adapters:** File storage (`local` / `s3`) and computer vision (`fake` / `roboflow`) are selected by configuration.

---

## Team

| Member           | Codigo                       |
|------------------|----------------------------------------|
| Giovany Torres   | u202311601                    |
| Fabrizio Cutiri  | u201914181                    |
| Franco Lopez     | u202315890                    |
| Yair Aru         | u202125984                    |
| Joaquín Carranza | u20191B935                    |

---

## Documentation

Refer to the project root for additional information:
- [Contributing Guide](CONTRIBUTING.md)
- [Dockerfile](Dockerfile) and [GitHub Actions workflows](.github/workflows/)
- [Changelog](CHANGELOG.md)
- Interactive API reference in Swagger UI

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
