#!/usr/bin/env bash

set -e

ENV_FILE="$HOME/.config/smartdirect/.env"

echo "Stopping existing containers..."
docker compose --env-file "$ENV_FILE" down --remove-orphans

echo "Starting containers..."
docker compose --env-file "$ENV_FILE" up -d --force-recreate

echo
docker compose ps