## 1. Backend Contract

- [x] 1.1 Define Dashboard statistics response DTOs for summary metrics, login trend buckets, recent operation log items, and sample time
- [x] 1.2 Add an administrator-only Dashboard statistics controller endpoint using the project's existing API response and security conventions
- [x] 1.3 Add code comments documenting statistic semantics for today's new users, notification count, operation log count, and successful-login trend

## 2. Backend Data Aggregation

- [x] 2.1 Implement Dashboard statistics service that aggregates user total count and today's new user count from the user module
- [x] 2.2 Implement notification count aggregation through the notification module's public query surface or a new narrow query method
- [x] 2.3 Implement operation log count aggregation through the audit module's public query surface
- [x] 2.4 Implement successful-login trend aggregation from existing login/auth records or add the minimal persistence/query support needed for successful login buckets
- [x] 2.5 Implement recent operation log aggregation from audit logs with bounded result size and backend-side sensitive field masking

## 3. Frontend API Integration

- [x] 3.1 Add Dashboard statistics API client and TypeScript types under the existing frontend API service structure
- [x] 3.2 Replace Dashboard summary card static data with API-driven state
- [x] 3.3 Replace Dashboard trend chart static data with backend login trend buckets
- [x] 3.4 Replace recent activity/static sales-style content with recent operation log data
- [x] 3.5 Add loading, empty, unauthorized, and failed-request states without silently showing hard-coded success metrics

## 4. Boundary And Observability Alignment

- [x] 4.1 Ensure Dashboard implementation does not depend on observability operations APIs for user, notification, login, or audit business summaries
- [x] 4.2 Preserve observability pages for system status, API monitoring, and security events without mixing Dashboard business statistics into those pages

## 5. Verification

- [x] 5.1 Verify the Dashboard statistics endpoint returns real values, zero/empty values for empty sources, and rejects unauthorized access
- [x] 5.2 Verify recent operation logs do not expose token, password, authorization, captcha, secret, key, raw request body, or equivalent sensitive values
- [x] 5.3 Verify the frontend Dashboard renders success, loading, empty, and failed-request states from API state
- [x] 5.4 Run the relevant backend and frontend checks used by this project for the touched modules
