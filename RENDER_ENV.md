# Render backend setup

Create a Render Web Service for `Ai-Life-Tracker-main` using the repository Dockerfile, or use the `render.yaml` Blueprint in this directory.

Set these environment variables in Render:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<supabase-database-password>
GROQ_API_KEY=<new-groq-api-key>
CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>,https://<your-custom-domain>
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
DB_POOL_SIZE=5
```

Render provides `PORT` automatically. The application binds to `0.0.0.0` and uses that port.

Health check path:

```text
/health
```

For local development, omit the Render variables and the defaults in `application.properties` use local PostgreSQL and localhost origins.

Never commit database passwords or Groq keys. Rotate the Groq key previously stored in the repository before deploying.
