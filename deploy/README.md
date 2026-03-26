# VDS Deployment

## Deployment Model

This project uses a simple learning-friendly CI/CD flow:

1. GitHub Actions builds the backend Docker image
2. GitHub Actions pushes the image to GHCR
3. GitHub Actions connects to the VDS over SSH
4. The VDS pulls the latest image and restarts the containers with Docker Compose

## Files

- `server/Dockerfile` builds the Ktor backend image
- `deploy/docker-compose.vds.yml` defines the VDS runtime stack
- `deploy/.env.example` documents the required server environment variables
- `.github/workflows/deploy-server.yml` runs the CI/CD pipeline

## VDS Prerequisites

Install on the server:

- Docker Engine
- Docker Compose plugin

Create a deployment directory, for example:

```bash
mkdir -p /opt/appshoptour
```

Create a real `.env` file there based on `deploy/.env.example`.

## GitHub Secrets

Create these repository secrets:

- `VDS_HOST`
- `VDS_PORT`
- `VDS_USER`
- `VDS_SSH_KEY`
- `VDS_APP_DIR`
- `GHCR_USERNAME`
- `GHCR_TOKEN`

Notes:

- `GHCR_TOKEN` should have package read access for the server pull step
- `VDS_APP_DIR` is the absolute directory on the server, for example `/opt/appshoptour`

## First-Time Server Setup

Copy the compose file from the repository to the VDS and create `.env` on the server:

```bash
cp docker-compose.vds.yml /opt/appshoptour/docker-compose.yml
cp .env.example /opt/appshoptour/.env
```

Then edit `.env` with real values.

## Workflow Behavior

The workflow deploys on:

- push to `main`
- manual trigger through GitHub Actions
