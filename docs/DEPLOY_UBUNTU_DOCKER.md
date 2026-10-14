# PayTrack production deployment (Ubuntu + Docker)

## 1. Server prerequisites

Recommended: Ubuntu Server 24.04 LTS with a public IPv4 address and a DNS record such as `api.example.com` pointing to it.

Open only these inbound ports:

- 22/tcp (SSH; ideally restricted by source IP)
- 80/tcp (HTTP, used by Caddy for ACME/redirects)
- 443/tcp (HTTPS)

Do not expose PostgreSQL 5432 publicly.

## 2. Install Docker Engine

Install Docker Engine and the Compose plugin from Docker's official Ubuntu repository, then verify:

```bash
docker --version
docker compose version
```

## 3. Clone and configure

```bash
sudo mkdir -p /opt/paytrack
sudo chown "$USER":"$USER" /opt/paytrack
cd /opt/paytrack
git clone https://github.com/yuceloper/paytrack-backend.git .
cp .env.example .env
```

Edit `.env` and set at minimum:

```dotenv
API_DOMAIN=api.example.com
POSTGRES_DB=paytrack
POSTGRES_USER=paytrack
POSTGRES_PASSWORD=<long-random-password>
JWT_SECRET=<long-random-secret-at-least-32-bytes>
GOOGLE_CLIENT_ID=<google-oauth-client-id-if-used>
```

Generate secrets, for example:

```bash
openssl rand -base64 48
```

## 4. Start the stack

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Caddy obtains and renews TLS certificates automatically after DNS points to the server and ports 80/443 are reachable.

## 5. Verify

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f caddy
```

Then test the public endpoint:

```bash
curl -i https://api.example.com/
```

Use the actual API endpoint path for a functional application test if `/` is not mapped.

## 6. Deploy updates

```bash
cd /opt/paytrack
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

## 7. Database backup

Example logical backup:

```bash
mkdir -p /opt/paytrack/backups
docker exec paytrack-postgres pg_dump -U paytrack -d paytrack -Fc \
  > /opt/paytrack/backups/paytrack-$(date +%F-%H%M).dump
```

Keep backups outside the Docker volume as well (another server/object storage).

## 8. Mobile release configuration

Build the Flutter application with the public HTTPS API URL:

```bash
flutter build ipa --release \
  --dart-define=API_BASE_URL=https://api.example.com
```

Never publish an App Store build that points to a LAN IP or plain HTTP development endpoint.
