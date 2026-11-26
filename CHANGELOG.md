# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2026-11-26

This is the final release of the project. It contains breaking changes for API clients: read the **BREAKING** items before updating the frontend or the mobile app.

### Added
- **IAM**
    - Sign in with Google: `POST /api/v1/authentication/google-sign-in` with a Google ID token (`GOOGLE_CLIENT_ID`).
- **Profiles & Organization**
    - The owner grants a clinic temporary access (1 to 72 hours) to the history recorded by other clinics, renews it and revokes it (`/api/v1/pets/{petId}/history-access-grants`).
- **AI Assistance**
    - AI credits: each vision analysis uses 1 computer vision credit and each LLM answer uses 1 LLM credit; the credit is released if the model fails.
- **Platform**
    - `SWAGGER_ENABLED` turns off Swagger UI and the OpenAPI document in production.
    - Startup validation: the API does not start if a Stripe, Google, OpenAI or Roboflow setting is missing, and it names each one.

### Changed
- **BREAKING – Profiles**: registering a pet or a veterinarian, including the clinic holder's veterinarian profile, requires an active subscription (`403`) and respects the plan limits (`409`). Accounts without a subscription must activate a plan first.
- **BREAKING – AI Assistance**: requires an active subscription and available credits (`403` / `409`).
- Clinical Attention, Clinical Exams, Medical History and AI Assistance show the records of other clinics only with an active access grant of the owner. Allergies are always visible.
- The AI assistant uses as backgrounds only the consultations the veterinarian can see.
- **BREAKING – Configuration**: `GOOGLE_CLIENT_ID`, `OPENAI_API_KEY`, `ROBOFLOW_API_KEY`, `ROBOFLOW_SKIN_MODEL_ID` and `ROBOFLOW_RADIOGRAPH_MODEL_ID` are required. OpenAI and Roboflow are always used outside the tests.
- **BREAKING – Subscriptions**: simulated payments are off by default also in `dev`; set `SUBSCRIPTIONS_SIMULATED_PAYMENTS_ENABLED=true` only to seed data.

### Removed
- **BREAKING**: the fake LLM, the fake vision model and the demo pets (`AI_LLM_PROVIDER=fake`, `AI_VISION_PROVIDER=fake`, `AI_ACL_MODE=stub`). They now exist only in the automated tests.

### Security
- **BREAKING**: registering a clinic patient requires the `petCode` shared by the owner (`400` without it, `403` with a wrong code).
- A clinic no longer sees the consultations, exams, vaccinations and timeline entries recorded by other clinics without the owner's permission.
- Test suite with 486 tests on an in-memory H2 database.

## [1.4.0] - 2026-11-25

### Added
- **AI Assistance**
    - LLM clinical assistant: SOAP draft for the veterinarian with the pet's backgrounds, regenerations, decision (accept, edit, reject) and PDF report of the confirmed draft.
    - Plain-language explanations for the pet owner about a consultation.
    - `fake` and `openai` LLM providers (`AI_LLM_PROVIDER`).
- **Clinical Attention**
    - Consultation summaries for other contexts (backgrounds of the AI assistant).
- **Subscriptions**
    - Simulated payments for development and demos (`SUBSCRIPTIONS_SIMULATED_PAYMENTS_ENABLED`).

### Changed
- OpenPDF is used again for the AI clinical report.
- Test suite with 450 tests on an in-memory H2 database.

## [1.3.0] - 2026-11-25

### Added
- **Medical History**
    - New bounded context: allergies, vaccinations and a timeline per pet.
    - The timeline is filled from completed consultations and validated exam results.
- **Clinical Exams**
    - `ExamResultValidatedIntegrationEvent` when a veterinarian validates a result.

### Changed
- The consultation completed integration event includes the clinic of the consultation.
- Test suite with 372 tests on an in-memory H2 database.

## [1.2.0] - 2026-11-25

### Added
- **Clinical Attention**
    - Treatments and prescriptions (1 to 15 medications) in a consultation in progress.
    - The consultation response includes `treatments` and `prescriptions`.
- Test suite with 359 tests on an in-memory H2 database.

## [1.1.0] - 2026-11-25

### Added
- **Subscriptions**
    - Plans for pet owners and clinics (`PET_OWNER_BASIC`, `PET_OWNER_PREMIUM`, `CLINIC_BASIC`, `CLINIC_PREMIUM`).
    - Subscriptions per account with Stripe checkout, webhook synchronization, payment history and invoices.
    - Scheduled cancellation and reactivation.
    - Plan entitlements, registration limits and monthly AI credits with reservations.

- **Tests**
    - Stripe webhook signatures verified with the real Stripe library (wrong secret, tampered body, replayed signature).
    - Payment flow with Stripe test doubles: checkout, `checkout.session.completed` → `ACTIVE`, duplicated events, `PAST_DUE`, wrong plan and cancellation.
    - Startup validation of the Stripe settings.
    - Opt-in live tests against Stripe test mode, enabled only when `STRIPE_SECRET_KEY` starts with `sk_test_`.
- **CI**
    - The test job passes the Stripe test-mode keys from the repository secrets to the live tests when they are configured.

### Changed
- `GET /api/v1/plans` is public.
- The API validates the `STRIPE_*` variables at startup and does not start if one is missing or malformed, naming each problem.
- Stripe checkout returns to the frontend (`/subscription?checkout=success|cancelled`) by default.

### Removed
- Empty checkout controller and resource of Subscriptions.
- Test suite with 345 tests on an in-memory H2 database.

## [1.0.2] - 2026-11-12

### Changed
- CD pipeline builds the Docker image on the GitHub runner and pushes it to Azure Container Registry (ACR Tasks are not available on Azure for Students).

## [1.0.1] - 2026-11-12

### Fixed
- Swagger UI and generated URLs now use HTTPS behind the Azure Container Apps ingress (`server.forward-headers-strategy=framework`).

### Changed
- Project version set to `1.0.1` in `pom.xml`.

## [1.0.0] - 2026-11-11

First stable release, delivered at the end of **Sprint 1**.

### Added
- **IAM**
    - Sign-up for pet owner and clinic accounts, and sign-in with JWT.
    - Password recovery by e-mail with single-use, expiring tokens.
    - Clinic members with a temporary password, activation and deactivation.
    - Roles `ROLE_PET_OWNER`, `ROLE_CLINIC_ADMIN` and `ROLE_VETERINARIAN`.
- **Profiles & Organization**
    - Pet owners and pets, with a unique pet code and photo.
    - Clinics, veterinarians and clinic patients.
    - Consulting rooms and assignment of veterinarians to rooms.
- **Clinical Attention**
    - Consultation lifecycle: `CREATED`, `IN_PROGRESS`, `COMPLETED` and `CANCELLED`.
    - Anamnesis, physiological assessments, clinical findings, diagnoses and re-evaluations.
- **Clinical Exams**
    - Exam requests from a consultation, start, result registration, validation, completion and cancellation.
    - Image, video and PDF attachments up to 10 MB, stored locally or in Amazon S3.
    - Clinic worklist with filters.
- **AI Assistance**
    - Computer vision analyses for skin photos (object detection) and X-rays (classification), linked to the pet and to the consultation.
    - Offline `fake` provider for development and demos.
- **Platform**
    - Multi-stage `Dockerfile` (Maven + Eclipse Temurin 25 JRE) running as a non-root user.
    - `prod` profile ready for Azure Container Apps and Azure Database for MySQL, with files stored on Amazon S3.
    - GitHub Actions: continuous integration on every pull request and deployment to Azure on release tags.
    - Swagger UI documentation.
    - Global error handling with a common error format.
    - Test suite with 302 tests on an in-memory H2 database.

### Changed
- Upgraded to **Java 25 (LTS)** and **Spring Boot 4.1.2**.
- Upgraded jjwt to 0.13.0, springdoc-openapi to 3.2.0 and AWS SDK to 2.58.0.

### Removed
- Unused OpenPDF dependency.
- Temporary S3 test endpoint (`/api/v1/test/s3`).

### Security
- Passwords are hashed with BCrypt.
- The `prod` profile does not start without an explicit `JWT_SECRET`.
- Access rules between contexts: a veterinarian only works with patients of their clinic, and an owner only sees their own pets.
- Owners only see exam results after a veterinarian validates them.

[Unreleased]: https://github.com/NexoShift/petcarehub-web-services/compare/v2.0.0...develop
[2.0.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.4.0...v2.0.0
[1.4.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.2
[1.0.1]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.1
[1.0.0]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.0
