## MODIFIED Requirements

### Requirement: Operations frontend pages

The system SHALL provide backend administrator pages for system status, API monitoring and security events, while keeping Dashboard business statistics out of the observability operations pages.

#### Scenario: Administrator opens system status page

- **WHEN** an authorized administrator opens the system status page
- **THEN** the page displays health cards, resource usage, dependency status and sample time using existing UI components

#### Scenario: Administrator opens API monitoring page

- **WHEN** an authorized administrator opens the API monitoring page
- **THEN** the page displays latency summary, error trend chart and slow endpoint table

#### Scenario: Administrator opens security event page

- **WHEN** an authorized administrator opens the security event page
- **THEN** the page displays login failure statistics, abnormal IP records, force logout records and related notification or audit entry links when available

#### Scenario: Administrator opens business Dashboard page

- **WHEN** an authorized administrator opens the business Dashboard page for user, notification, file upload, login trend, and recent operation summaries
- **THEN** the page uses the Dashboard statistics API rather than observability operations APIs
