#!/usr/bin/env bash
# Builds and starts the full UbiGrid local architecture: the app itself (API +
# static front-end), Floci (local AWS emulator), MongoDB, Redis and
# PostgreSQL, all as containers on one Docker network. See README.md for
# details on each piece.
#
# Usage: ./start.sh

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

COMPOSE_FILE="config/docker-compose.yml"
SERVICES=(ubigrid-floci ubigrid-mongodb ubigrid-redis ubigrid-postgres ubigrid-app)

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required but was not found in PATH." >&2
    exit 1
fi

echo "==> Building and starting the UbiGrid stack..."
docker compose -f "$COMPOSE_FILE" up -d --build

echo "==> Waiting for every container to report healthy (this can take a minute on the first run)..."
for service in "${SERVICES[@]}"; do
    printf "    %-16s " "$service"
    status="starting"
    for _ in $(seq 1 90); do
        status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "starting")
        if [ "$status" = "healthy" ]; then
            echo "healthy"
            break
        fi
        sleep 2
    done
    if [ "$status" != "healthy" ]; then
        echo "TIMED OUT (last status: $status)"
        echo
        echo "Something didn't come up. Inspect it with:"
        echo "  docker compose -f $COMPOSE_FILE logs $service"
        exit 1
    fi
done

cat <<EOF

==> UbiGrid is up.

  Acceso directo (autologin)
    >>> http://localhost:8080/request.html?autologin=natalia@mail.com

  Front-end
    Landing           http://localhost:8080/
    Registro          http://localhost:8080/register.html
    Solicitar vehículo http://localhost:8080/request.html

  API y dependencias
    UbiGrid API       http://localhost:8080
    Actuator health   http://localhost:8080/actuator/health
    Swagger UI        http://localhost:8080/swagger-ui.html
    Floci (AWS)       http://localhost:4566
    MongoDB           localhost:27017
    Redis             localhost:6380
    PostgreSQL        localhost:5435

Logs:    docker compose -f $COMPOSE_FILE logs -f ubigrid
Stop:    docker compose -f $COMPOSE_FILE down
Reset:   docker compose -f $COMPOSE_FILE down -v   (also wipes persisted data)
EOF
