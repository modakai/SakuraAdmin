# observability-ops-panel Specification

## Purpose
TBD - created by archiving change add-observability-ops-panel. Update Purpose after archive.
## Requirements
### Requirement: Observability module boundary

The system SHALL provide observability and operations features through an independent Spring Modulith module named `observability`.

#### Scenario: Module metadata exists

- **WHEN** the backend application starts with Spring Modulith metadata scanning
- **THEN** the `observability` module is identified as an application module with a Chinese display name and explicit allowed dependencies

#### Scenario: Module avoids internal notification dependency

- **WHEN** observability needs to create a system notification from an alert
- **THEN** it SHALL publish an application event or call a formal named interface instead of directly invoking notification module internal services

### Requirement: Admin system status aggregation

The system SHALL provide an administrator-only system status API that aggregates JVM, memory, CPU, disk, database and Redis status into backend-admin-friendly response models.

#### Scenario: Administrator views system status

- **WHEN** an authorized administrator requests the system status page data
- **THEN** the system returns JVM memory, thread, GC, CPU, memory, disk, database and Redis status with status levels and sample time

#### Scenario: Dependency is unavailable

- **WHEN** database or Redis health check fails
- **THEN** the system returns a degraded or down status for that dependency without failing the whole status response

### Requirement: Actuator endpoint protection

The system SHALL use Actuator as a backend metrics source while preventing the frontend from depending on raw Actuator endpoint responses.

#### Scenario: Frontend requests operations data

- **WHEN** the frontend loads observability pages
- **THEN** it calls `/admin/observability/**` APIs rather than raw `/actuator/**` endpoints

#### Scenario: Production actuator exposure is restricted

- **WHEN** production configuration is active
- **THEN** only explicitly configured Actuator endpoints are exposed and sensitive endpoint data is not available to browser clients

### Requirement: API latency and slow endpoint statistics

The system SHALL collect API latency statistics and expose slow endpoint records for administrator review.

#### Scenario: Slow request is completed

- **WHEN** a backend request exceeds the configured slow API threshold
- **THEN** the system records a slow endpoint event with method, path, status, duration, IP, user context if available, and occurrence time

#### Scenario: Administrator views slow endpoints

- **WHEN** an authorized administrator filters slow endpoints by time range or path
- **THEN** the system returns a paginated list ordered by occurrence time or duration

### Requirement: API error trend statistics

The system SHALL aggregate API error trends by time bucket, status category and exception summary.

#### Scenario: Error response is completed

- **WHEN** a request returns a server error or handled exception
- **THEN** the system records an error event with sanitized exception summary and status code

#### Scenario: Administrator views error trend

- **WHEN** an authorized administrator selects a time range
- **THEN** the system returns time-bucketed 4xx, 5xx and exception counts suitable for chart rendering

### Requirement: Login failure and abnormal IP statistics

The system SHALL track login failure counts and identify abnormal IP activity using configurable time windows and thresholds.

#### Scenario: Login failure threshold is exceeded

- **WHEN** the same IP or account exceeds the configured login failure threshold within the configured window
- **THEN** the system records a security event and marks the source as abnormal for administrator review

#### Scenario: Administrator views abnormal IP list

- **WHEN** an authorized administrator opens the security events page
- **THEN** the system returns abnormal IP records with failure count, related account count, last failure time and status

### Requirement: Force logout record visibility

The system SHALL expose force logout records as part of the security event view.

#### Scenario: User is force logged out

- **WHEN** an administrator forces an online user session offline
- **THEN** the system records or aggregates a force logout event containing operator, target user, target session, IP if available and operation time

#### Scenario: Administrator reviews force logout records

- **WHEN** an authorized administrator filters security events by force logout type
- **THEN** the system returns matching records with enough context to audit the operation

### Requirement: Audit and notification linkage

The system SHALL link high-risk observability events with audit logging and system notification generation.

#### Scenario: High-frequency login failure alert is raised

- **WHEN** login failure activity exceeds the configured alert threshold
- **THEN** the system writes an audit fact or event and emits an alert event that can create a system notification

#### Scenario: Alert notification is deduplicated

- **WHEN** the same alert rule and subject are triggered repeatedly within the configured cooldown window
- **THEN** the system creates at most one system notification for that rule and subject during the cooldown window

### Requirement: Operations frontend pages

The system SHALL provide backend administrator pages for system status, API monitoring and security events.

#### Scenario: Administrator opens system status page

- **WHEN** an authorized administrator opens the system status page
- **THEN** the page displays health cards, resource usage, dependency status and sample time using existing UI components

#### Scenario: Administrator opens API monitoring page

- **WHEN** an authorized administrator opens the API monitoring page
- **THEN** the page displays latency summary, error trend chart and slow endpoint table

#### Scenario: Administrator opens security event page

- **WHEN** an authorized administrator opens the security event page
- **THEN** the page displays login failure statistics, abnormal IP records, force logout records and related notification or audit entry links when available

### Requirement: Sensitive data protection

The system SHALL sanitize observability events before storage and presentation.

#### Scenario: Request context contains sensitive fields

- **WHEN** request context, headers, parameters or exception details include token, password, authorization, captcha, secret or key data
- **THEN** the system masks or removes those values before storing or returning observability data

#### Scenario: Exception stack trace exists

- **WHEN** an exception includes a full stack trace
- **THEN** the system stores only a bounded exception type and message summary in observability event records

