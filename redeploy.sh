#!/usr/bin/env bash
# Rebuilds only the UbiGrid app image and recreates its container, leaving
# Floci, MongoDB, Redis and PostgreSQL untouched. Use it after changing the
# front-end (static/) or the Java code; use ./start.sh for a first run.
#
# Usage: ./redeploy.sh [email]
#   email  account for the autologin link (default: natalia@mail.com)

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

COMPOSE_FILE="config/docker-compose.yml"
SERVICE="ubigrid"
CONTAINER="ubigrid-app"
EMAIL="${1:-natalia@mail.com}"

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required but was not found in PATH." >&2
    exit 1
fi

echo "==> Rebuilding the $SERVICE image..."
docker compose -f "$COMPOSE_FILE" build "$SERVICE"

echo "==> Recreating $CONTAINER (dependencies are started only if they are down)..."
docker compose -f "$COMPOSE_FILE" up -d "$SERVICE"

printf "==> Waiting for %s to report healthy " "$CONTAINER"
status="starting"
for _ in $(seq 1 90); do
    status=$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null || echo "starting")
    if [ "$status" = "healthy" ]; then
        break
    fi
    printf "."
    sleep 2
done
echo

if [ "$status" != "healthy" ]; then
    echo "TIMED OUT (last status: $status)"
    echo "Inspect it with: docker compose -f $COMPOSE_FILE logs $SERVICE"
    exit 1
fi

cat <<OUT

==> UbiGrid redeployed.

  Acceso directo (autologin)
    >>> http://localhost:8080/request.html?autologin=$EMAIL

Logs:  docker compose -f $COMPOSE_FILE logs -f $SERVICE
OUT
