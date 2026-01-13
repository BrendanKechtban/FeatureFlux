# FeatureFlux - Distributed Feature Flag Platform BK

Welcome to FeatureFlux. A production-ready feature flag platform built with Spring Boot, Redis, PostgreSQL, and Next.js. Enables real-time feature toggles and instant rollback without application redeployment.

## Features

- **Real-time Feature Toggles**: Enable/disable features instantly without redeployment
- **Deterministic User Bucketing**: Consistent percentage-based rollouts using SHA-256 hashing
- **Redis-Backed Caching**: High-performance evaluation engine with Redis caching
- **PostgreSQL Persistence**: Reliable data storage with JPA/Hibernate
- **Admin Dashboard**: Full-stack Next.js dashboard for feature management
- **Live Status Monitoring**: Real-time updates and status tracking
- **Staged Rollouts**: Controlled percentage-based deployments (0-100%)
- **User Targeting**: Include/exclude specific users from feature flags

## Architecture

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Next.js   │────────▶│ Spring Boot │────────▶│ PostgreSQL  │
│  Frontend   │         │   Backend   │         │  Database   │
└─────────────┘         └─────────────┘         └─────────────┘
                               │
                               ▼
                        ┌─────────────┐
                        │    Redis    │
                        │   Cache     │
                        └─────────────┘
```

## Tech Stack

### Backend
- Spring Boot
- PostgreSQL (persistent storage)
- Redis (caching layer)
- Spring Data JPA
- Spring WebSocket (for real-time updates)

### Frontend
- Next.js 
- TypeScript
- Tailwind CSS
- React Query (for data fetching)


## Deterministic User Bucketing

The platform uses SHA-256 hashing to ensure consistent user bucketing:

1. Combines flag key + user ID
2. Generates SHA-256 hash
3. Maps to bucket 0-99
4. Same user always gets same bucket for same flag

This ensures:
- Consistent experience for users
- Predictable rollout percentages
- No user migration between buckets

## Performance

- **Redis Caching**: Evaluation results cached for 60 seconds
- **Database Offloading**: High-frequency reads served from Redis
- **Low Latency**: Sub-millisecond evaluation times
- **Scalability**: Horizontal scaling supported


## Contributing

Contributions welcome! Please open an issue or submit a pull request.

