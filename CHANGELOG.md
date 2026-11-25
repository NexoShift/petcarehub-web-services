# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.2.0...develop
[1.2.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.2
[1.0.1]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.1
[1.0.0]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.0
