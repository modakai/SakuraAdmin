## ADDED Requirements

### Requirement: Login audit logging
The system SHALL record a login audit log entry for each login attempt, including account identifier, user ID when available, IP address, client information, login result, failure reason when applicable, elapsed time, and audit time.

#### Scenario: Successful login is recorded
- **WHEN** a user logs in with valid credentials
- **THEN** the system creates a login audit log entry with result `SUCCESS`, the resolved user ID, account identifier, IP address, client information, elapsed time, and audit time

#### Scenario: Failed login is recorded
- **WHEN** a login attempt fails because the account, password, status, or verification input is invalid
- **THEN** the system creates a login audit log entry with result `FAILURE`, account identifier, IP address, client information, failure reason, elapsed time, and audit time

### Requirement: Administrator operation audit logging
The system SHALL record administrator operation audit log entries for audited backend operations, including request path, HTTP method, IP address, user ID, account identifier, elapsed time, result, status code, exception summary when applicable, and audit time.

#### Scenario: Successful admin operation is recorded
- **WHEN** an audited administrator operation completes successfully
- **THEN** the system creates an administrator operation audit log entry with the request metadata, administrator identity, elapsed time, result `SUCCESS`, status code, and audit time

#### Scenario: Failed admin operation is recorded
- **WHEN** an audited administrator operation throws an exception or returns a failure result
- **THEN** the system creates an administrator operation audit log entry with result `FAILURE` and an exception or failure summary

### Requirement: Annotation based operation description
The system SHALL provide an audit annotation for important operations so backend methods can declare operation description, business module, operation type, and request or response summary preferences.

#### Scenario: Annotated operation uses business description
- **WHEN** an administrator invokes a method annotated with an audit description such as "删除用户"
- **THEN** the corresponding administrator operation audit log stores that operation description with the request metadata

#### Scenario: Unannotated operation is not logged as important operation
- **WHEN** an administrator invokes a backend method that is not covered by audit logging rules
- **THEN** the system does not create an important operation audit log entry for that method

### Requirement: Request and response audit fields
The system SHALL support storing request summary, response summary, trace ID, business module, operation type, status code, request path, HTTP method, IP address, user ID, elapsed time, result, and exception summary for audit records.

#### Scenario: Request metadata is captured
- **WHEN** an audited request is processed
- **THEN** the system stores the request path, HTTP method, IP address, user ID when available, elapsed time, result, and audit time in the audit record

#### Scenario: Exception details are summarized
- **WHEN** an audited request fails with an exception
- **THEN** the system stores a bounded exception summary without storing a full stack trace in the audit record

### Requirement: Sensitive data protection
The system MUST mask or omit sensitive request and response fields before audit data is persisted or exported, including passwords, tokens, authorization values, captchas, secrets, and keys.

#### Scenario: Sensitive request values are masked
- **WHEN** an audited request contains a password, token, authorization value, captcha, secret, or key field
- **THEN** the persisted request summary and exported audit data do not contain the raw sensitive value

#### Scenario: Oversized summaries are bounded
- **WHEN** a request summary, response summary, or exception summary exceeds the configured length limit
- **THEN** the system stores only a bounded summary that preserves audit usefulness without exceeding the field limit

### Requirement: Audit log query
The system SHALL provide a backend audit log query interface that supports pagination and filtering by log type, user ID, account identifier, IP address, request path, HTTP method, result, operation description, business module, operation type, and audit time range.

#### Scenario: Filter audit logs by time and result
- **WHEN** an administrator filters audit logs by audit time range and result
- **THEN** the system returns only matching audit records in paginated order

#### Scenario: Filter audit logs by request metadata
- **WHEN** an administrator filters audit logs by request path, HTTP method, IP address, or user ID
- **THEN** the system returns only audit records matching the selected request metadata

### Requirement: Audit log detail
The system SHALL provide a backend audit log detail interface for authorized administrators to view one audit record, including metadata, operation description, request summary, response summary, and exception summary.

#### Scenario: View audit detail
- **WHEN** an authorized administrator opens an audit log detail
- **THEN** the system returns the selected audit record with its available metadata and masked summaries

### Requirement: Audit log export
The system SHALL provide a backend audit log export capability that uses the current query filters, enforces export limits, and exports only fields allowed for audit review.

#### Scenario: Export filtered audit logs
- **WHEN** an authorized administrator exports audit logs after applying filters
- **THEN** the system exports only audit records matching those filters and within the configured export limit

#### Scenario: Export excludes raw sensitive values
- **WHEN** exported audit logs include request or response summaries
- **THEN** the exported file contains masked summaries and does not contain raw sensitive values

### Requirement: Audit access control
The system MUST restrict audit log query, detail, and export operations to administrators with audit log permissions.

#### Scenario: Unauthorized user is denied
- **WHEN** a user without audit log permission requests audit log query, detail, or export
- **THEN** the system denies the request and does not return audit data

### Requirement: Audit write failure isolation
The system SHALL ensure audit log write failures do not change the result of the original business request.

#### Scenario: Audit persistence fails
- **WHEN** the original business request succeeds but asynchronous audit persistence fails
- **THEN** the original request result remains successful and the audit failure is recorded in application diagnostics

### Requirement: Backend audit log page
The frontend SHALL provide a backend audit log page where authorized administrators can filter, inspect, and export login logs and administrator operation logs.

#### Scenario: Administrator filters audit log list
- **WHEN** an authorized administrator opens the audit log page and applies filters
- **THEN** the page displays paginated audit records returned by the backend for those filters

#### Scenario: Administrator exports current result set
- **WHEN** an authorized administrator clicks export from the audit log page
- **THEN** the frontend sends the current filters to the export interface and presents the export result to the administrator
