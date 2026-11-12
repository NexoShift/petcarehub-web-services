# Quality Attributes

This document lists the quality attribute scenarios defined for **PetCare Hub** (report section 4.1.5, *Quality Attribute Scenario Refinements*) and describes how release **1.0.0** of the backend addresses each one: the architectural tactics applied, where they live in the code and their current status.

## Summary

| # | Quality attribute | Scenario | Response measure | Status in 1.0.0 |
|---|-------------------|----------|------------------|-----------------|
| QA-1 | Performance | LLM clinical assistance during a consultation | 95 % of LLM responses in ≤ 10 s | 🕓 Planned (Sprint 2) |
| QA-2 | Availability | Clinical features keep working when an AI provider fails | 100 % of core clinical features available during isolated AI provider failures | ✅ Implemented for computer vision |
| QA-3 | Security | Users only access resources allowed for their role and relationship | 100 % of unauthorized requests rejected without exposing data | ✅ Implemented |
| QA-4 | Integrity | Re-evaluations keep previous clinical records | 100 % of re-evaluations preserve the original record and its traceability | ✅ Implemented |

Additional non-functional requirements from the report:

| Requirement | Target | Status in 1.0.0 |
|-------------|--------|-----------------|
| Computer vision response time | ≤ 8 s (p95) | ✅ 8 s timeout on the Roboflow client |
| Computer vision precision | ≥ 85 % | 🔬 Measured on the Roboflow validation set, not by the API |
| LLM context limited to what the veterinarian selects | Only selected records are sent | 🕓 Planned with QA-1 |

---

## QA-1: Performance — LLM clinical assistance

| Component | Description |
|-----------|-------------|
| **Source** | Authenticated veterinarian |
| **Stimulus** | Requests LLM clinical assistance during a consultation, using the current consultation data and the selected clinical background |
| **Artifact** | AI Assistance module, pet clinical history, external LLM service |
| **Environment** | Normal operation, external LLM available |
| **Response** | Validates the request, gathers the current consultation and the selected background, sends the context to the LLM and returns a structured draft as support information |
| **Response measure** | 95 % of LLM responses delivered in at most **10 seconds** |

**Tactics planned**
- Synchronous call with a **10 s timeout** (`ai.openai.timeout=10s`, already in the configuration).
- **Bounded context**: only the consultation and the records explicitly selected by the veterinarian are sent (`max-completion-tokens=1200`).
- The response is stored as a **draft labelled as AI-generated**; it becomes clinical information only after the veterinarian accepts or edits it.

**Status:** the LLM assistant is scheduled for **Sprint 2**. Release 1.0.0 already includes the configuration and the `fake` provider (`ai.llm.provider=fake`).

---

## QA-2: Availability — AI provider failure

| Component | Description |
|-----------|-------------|
| **Source** | Veterinarian or pet owner using an AI feature |
| **Stimulus** | Uses a feature while the external computer vision or LLM provider is temporarily unavailable |
| **Artifact** | AI Assistance module and external provider |
| **Environment** | Temporary or isolated failure of the external AI provider |
| **Response** | Detects the failure, isolates it from the clinical features and informs the user that AI assistance is temporarily unavailable |
| **Response measure** | Core clinical features remain available in **100 %** of isolated AI provider failures |

**Tactics applied**
- **Timeout**: the Roboflow client has an **8 s** read timeout (`ai.roboflow.timeout=8s`).
- **Fault isolation**: provider errors and timeouts are translated into `AiProviderUnavailableException`, which the global error handler returns as **`503 Service Unavailable`**. No partial or invented result is stored.
- **Bounded context isolation** (ADR-001, ADR-004): consultations, exams, profiles and IAM do not depend on AI Assistance, so they keep working when the provider is down.
- **Optional step**: the images step of the consultation is optional; a consultation can be completed without any AI analysis.
- **Degradation mode**: the `fake` provider (`AI_VISION_PROVIDER=fake`) allows working without the external service in development and demos.

**Verification:** unit tests of `VisionAnalysisCommandServiceImpl` cover the provider-unavailable case.

---

## QA-3: Security — authorized access only

| Component | Description |
|-----------|-------------|
| **Source** | Authenticated user without sufficient permissions |
| **Stimulus** | Tries to access clinical information or run an operation not allowed for them |
| **Artifact** | IAM module and the requested clinical resource |
| **Environment** | Normal operation, user authenticated |
| **Response** | Verifies identity, role and permissions; rejects the operation without returning, modifying or deleting protected data |
| **Response measure** | **100 %** of requests without valid permissions are rejected with a proper authorization response |

**Tactics applied**
- **Authenticate actors**: stateless JWT validated on every request; passwords hashed with BCrypt (ADR-006).
- **Authorize actors by role**: `ROLE_PET_OWNER`, `ROLE_CLINIC_ADMIN` and `ROLE_VETERINARIAN` checked with Spring Security.
- **Authorize by relationship**: a veterinarian only works with patients, consultations and exams of **their own clinic**; an owner only sees **their own pets** and **validated** exam results. These checks use the Profiles ACL facade.
- **Limit exposure**: unauthenticated requests return `401`, forbidden ones `403`, and errors never include internal data.
- **Protect secrets**: credentials are read from environment variables; the `prod` profile does not start without `JWT_SECRET`.
- **Limit access**: CORS only allows the configured web origins; the S3 test endpoint was removed before the release.

**Verification:** REST and service tests cover `401`/`403` responses and cross-clinic and cross-owner access. A manual audit of 108 API cases was run on `develop` before the release.

---

## QA-4: Integrity — clinical re-evaluations

| Component | Description |
|-----------|-------------|
| **Source** | Veterinarian |
| **Stimulus** | Registers a re-evaluation of a previous diagnosis after new evidence |
| **Artifact** | Clinical Attention module (`ClinicalConsultation` aggregate) |
| **Environment** | Normal operation |
| **Response** | Keeps the original diagnosis and adds the new conclusion linked to it and to the responsible veterinarian |
| **Response measure** | **100 %** of re-evaluations preserve the previous records and their traceability |

**Tactics applied**
- **Append-only records**: re-evaluations are new entities linked to the original diagnosis (`POST /clinical-consultations/{id}/diagnoses/{diagnosisId}/reevaluations`); the original is never overwritten (ADR-011).
- **State machine in the aggregate**: `CREATED → IN_PROGRESS → COMPLETED | CANCELLED`; completed or cancelled consultations reject any change with `409 Conflict`.
- **Traceability**: every record stores the responsible veterinarian and audit timestamps (`AuditableAbstractAggregateRoot`).
- **Validation before results reach owners**: exam results require a veterinarian's validation.

**Verification:** domain and REST tests of Clinical Attention cover re-evaluations and changes on closed consultations.

---

## How quality is checked continuously

- **302 automated tests** run on every pull request through GitHub Actions (`ci.yml`).
- No release is deployed to Azure unless all tests pass (`cd.yml`).
- Related decisions: see [ADRs](adrs.md).
