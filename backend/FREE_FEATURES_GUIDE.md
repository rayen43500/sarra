# Free Features Guide

This document describes the free MVP features that were added and how to use them.

## Global Notes

- API base URL: `http://localhost:8080`
- Security: JWT already required for `/api/client/**` and `/api/admin/**`
- Public routes: `/api/public/**`, `/oauth2/**`, `/login/oauth2/**`, `/ws-chat/**`

## Implemented Features

### 1) API Email (free mode)

- Endpoint: `POST /api/client/features/email`
- Request body:

```json
{
  "to": "user@example.com",
  "subject": "Hello",
  "body": "Test message"
}
```

- Behavior:
  - If SMTP is configured, it sends a real email.
  - Otherwise, returns simulated status.

### 2) API SMS (free simulated)

- Endpoint: `POST /api/client/features/sms`
- Request body:

```json
{
  "to": "+21600000000",
  "message": "Your code is 1234"
}
```

- Behavior: saves into `sms_logs` with `SIMULATED_SENT`.

### 3) Spring Batch

- Job: `newsletterDigestJob`
- Step: `newsletterDigestStep`
- Manual run endpoint (admin): `POST /api/admin/features/batch/newsletter-digest/run`
- Config:
  - `spring.batch.jdbc.initialize-schema=always`
  - `spring.batch.job.enabled=false`

### 4) Scheduler

- Task: mark old requested/confirmed appointments as `MISSED`
- Cron: every 30 minutes

### 5) AOP

- Aspect logs service execution time for methods in `org.example.backend.service..*`

### 6) Bad Words Filter

- Service: `BadWordsFilterService`
- Applied to messages, ratings comments, appointments notes, email/sms content.

### 7) Newsletter + RSS

- Subscribe: `POST /api/public/newsletter/subscribe`
- Unsubscribe: `POST /api/public/newsletter/unsubscribe`
- RSS feed: `GET /api/public/rss/certificates` (XML)

### 8) Like / Dislike

- React: `POST /api/client/features/reactions`
- Get summary: `GET /api/client/features/reactions?targetType=EXAM&targetId=1`

### 9) Chat WebSocket

- STOMP endpoint: `/ws-chat` (SockJS enabled)
- Send destination: `/app/chat.send`
- Subscribe destination: `/topic/public`

### 10) Simple Messaging (REST)

- Send message: `POST /api/client/features/messages`
- Get conversation: `GET /api/client/features/messages/{otherUserId}`

### 11) Rating

- Rate: `POST /api/client/features/ratings`
- Summary: `GET /api/client/features/ratings?targetType=EXAM&targetId=1`

### 12) Payment (free mock)

- Endpoint: `POST /api/client/features/payments`
- Generates simulated payment with status and reference.

### 13) Facebook/LinkedIn Auth (base ready)

- Provider info: `GET /api/public/social/providers`
- Requires OAuth2 credentials in properties to be fully active.

### 14) Appointment Booking (Prise de RDV)

- Client create: `POST /api/client/features/appointments`
- Client list: `GET /api/client/features/appointments`
- Admin list: `GET /api/admin/features/appointments`
- Admin status update: `PATCH /api/admin/features/appointments/{id}/status?status=CONFIRMED`

### 15) Ban User enforced

- `UserStatus.BLOCKED` now prevents login in `CustomUserDetailsService`.

## Suggested Next Steps

- Add OAuth2 credentials for Facebook and LinkedIn.
- Add frontend pages/hooks for features endpoints.
- Add integration tests for new controllers.
