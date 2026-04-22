## ADDED Requirements

### Requirement: Administrator dashboard statistics API
The system SHALL provide an administrator-only Dashboard statistics API that returns real backend statistics for the management home page instead of static frontend data.

#### Scenario: Administrator requests dashboard statistics
- **WHEN** an authorized administrator requests the Dashboard statistics API
- **THEN** the system returns user total count, today's new user count, notification count, operation log count, login trend data, recent operation logs, and backend sample time

#### Scenario: Unauthorized user requests dashboard statistics
- **WHEN** an unauthenticated or unauthorized user requests the Dashboard statistics API
- **THEN** the system rejects the request according to the existing authentication and authorization rules

### Requirement: Dashboard summary metric semantics
The system SHALL calculate Dashboard summary metrics on the backend with explicit server-side semantics.

#### Scenario: Summary metrics are calculated
- **WHEN** the Dashboard statistics API builds the summary response
- **THEN** user total count reflects all persisted users, today's new user count uses the server business day, notification count uses the chosen administrator-visible notification scope, and operation log count reflects persisted administrator operation audit logs

#### Scenario: No source records exist
- **WHEN** one or more statistic data sources have no matching records
- **THEN** the system returns zero-valued metrics and empty arrays rather than placeholder or mock values

### Requirement: Login trend data
The system SHALL return login trend data suitable for rendering the Dashboard trend chart.

#### Scenario: Login trend is requested
- **WHEN** the Dashboard statistics API returns login trend data
- **THEN** the response contains ordered time buckets with labels, start time, end time, and successful login count for each bucket

#### Scenario: Login trend source is empty
- **WHEN** no successful login records exist in the configured trend window
- **THEN** the system returns the expected time buckets with zero counts or an empty trend response that the frontend can render as an empty state

### Requirement: Recent operation log summary
The system SHALL return a bounded list of recent operation logs for the Dashboard without exposing sensitive request data.

#### Scenario: Recent operation logs are returned
- **WHEN** the Dashboard statistics API includes recent operation logs
- **THEN** each log item contains display-safe fields such as operator, action, target summary, result, IP address if available, and operation time

#### Scenario: Operation log contains sensitive values
- **WHEN** an operation log source includes token, password, authorization, captcha, secret, key, request body, or other sensitive values
- **THEN** the Dashboard response omits or masks those values before returning data to the frontend

### Requirement: Dashboard frontend uses backend data
The frontend Dashboard SHALL render summary cards, login trend, and recent operation logs from the backend Dashboard statistics API.

#### Scenario: Dashboard page loads successfully
- **WHEN** an authorized administrator opens the Dashboard page and the statistics API succeeds
- **THEN** the page displays backend-provided summary metrics, login trend data, and recent operation logs

#### Scenario: Dashboard API is loading
- **WHEN** the Dashboard page is waiting for the statistics API response
- **THEN** the page displays a loading state without showing stale static success data as if it were real

#### Scenario: Dashboard API fails
- **WHEN** the Dashboard statistics API request fails
- **THEN** the page displays an error or degraded state and does not silently fall back to hard-coded success metrics

### Requirement: Dashboard module boundary
The system SHALL keep Dashboard business statistics separate from observability operations monitoring.

#### Scenario: Dashboard reads cross-module statistics
- **WHEN** Dashboard statistics need data from user, notification, authentication, or audit modules
- **THEN** the Dashboard implementation uses public query services, repositories, or named interfaces rather than depending on unrelated module internals

#### Scenario: Operations metrics are needed
- **WHEN** administrators need JVM, CPU, dependency health, API latency, API error trend, or abnormal IP information
- **THEN** those metrics remain served by observability operations APIs rather than the Dashboard business statistics API
