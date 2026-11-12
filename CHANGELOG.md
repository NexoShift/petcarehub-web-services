# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- **Medical History**: longitudinal clinical record per pet, allergies and vaccinations.
- **Clinical Attention**: treatments, prescriptions and medications.
- **Subscriptions**: plans for owners and clinics, and AI usage limits.
- **AI Assistance**: LLM clinical assistant (SOAP draft) and plain-language explanations for owners.

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

[Unreleased]: https://github.com/NexoShift/petcarehub-web-services/compare/v1.0.1...develop
[1.0.1]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.1
[1.0.0]: https://github.com/NexoShift/petcarehub-web-services/releases/tag/v1.0.0
