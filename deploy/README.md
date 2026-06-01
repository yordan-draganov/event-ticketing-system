# Event Ticketing System

This directory contains the production infrastructure configuration for the Event Ticketing System, managed via Docker Compose.

## Core Responsibilities

The infrastructure configuration coordinates the orchestration, deployment settings, and runtime boundaries of the platform.

- Containerization: Configures the multi-service deployment with Docker Compose.
- Reverse Proxy: Uses Caddy to expose the frontend over HTTPS and proxy traffic into the Docker network.
- Network Isolation: Keeps PostgreSQL, Redis, and the backend off the public internet.
- Persistence: Stores database data, Redis data, Caddy certificates, and generated QR codes in Docker volumes.

## Installation

### 1. Getting Started

Open a terminal on the server and run:

```bash
git clone https://github.com/yordan-draganov/event-ticketing-system
cd event-ticketing-system
git switch -C feat/deployment origin/feat/deployment
```

### 2. Environment Variables Configuration

Copy the example deployment environment file and edit the values:

```bash
cp config/env/.env.example config/env/.env
nano config/env/.env
```

For the live deployment, configure:

```env
APP_DOMAIN=eventsly.app
APP_URL=https://eventsly.app
CORS_ALLOWED_ORIGINS=https://eventsly.app
VITE_API_BASE_URL=
```

Also set the database credentials, JWT/HMAC secrets, email credentials, and Stripe keys.

### 4. Stripe Webhook Configuration

In the Stripe Dashboard, create a webhook endpoint:

```text
https://eventsly.app/api/webhooks/stripe
```

Enable the payment intent events used by the application and copy the `whsec_...` signing secret into `STRIPE_WEBHOOK_SECRET`.

### 5. Running the Application

Start the production stack from the repository root:

```bash
docker compose --env-file config/env/.env -f deploy/docker-compose.prod.yml up -d --build
```

The application is available at:

```text
https://eventsly.app
```