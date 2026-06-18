# Deployment Guide

This project can run on Render, but the recommended split deployment is:

- Backend, PostgreSQL, and resume uploads: Railway
- Frontend static site: Vercel

## Railway Backend

1. Create a Railway project from the GitHub repository.
2. Select the backend service and set the root directory to `/backend`.
3. If Railway does not auto-detect it, set the config-as-code path to `/backend/railway.json`.
4. Add a PostgreSQL service in the same Railway project.
5. Add a volume to the backend service with mount path `/app/uploads`.
6. Add these backend service variables:

```text
CACHE_TYPE=simple
RABBIT_LISTENER_AUTO_STARTUP=false
JPA_DDL_AUTO=update
JWT_SECRET=<generate-a-long-random-secret>
FRONTEND_ORIGIN=https://<your-vercel-project>.vercel.app
PGHOST=${{Postgres.PGHOST}}
PGPORT=${{Postgres.PGPORT}}
PGDATABASE=${{Postgres.PGDATABASE}}
PGUSER=${{Postgres.PGUSER}}
PGPASSWORD=${{Postgres.PGPASSWORD}}
```

The Docker entrypoint builds the JDBC URL from the `PG*` variables and writes resumes to `${RAILWAY_VOLUME_MOUNT_PATH}/resumes`, which becomes `/app/uploads/resumes` with the volume above.

After deployment, generate a Railway public domain for the backend and verify:

```text
https://<your-railway-backend-domain>/api/health
```

## Vercel Frontend

1. Create a Vercel project from the same GitHub repository.
2. Set the project root directory to `frontend`.
3. Keep the default Vite build, or use the committed `frontend/vercel.json`.
4. Add this frontend environment variable:

```text
VITE_API_URL=https://<your-railway-backend-domain>/api
```

5. Deploy the frontend.
6. Go back to Railway and update `FRONTEND_ORIGIN` to the final Vercel production URL, then redeploy the backend.

## Optional Render Blueprint

The existing `render.yaml` is still available if you want an all-Render deployment later.
