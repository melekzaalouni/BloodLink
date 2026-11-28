#!/usr/bin/env bash
# Test « machine propre ». Usage : ./scripts/clean-machine-test.sh <url-du-depot> [branche]
# Arrête d'abord ta pile de travail (docker compose down) pour libérer les ports.
set -euo pipefail
REPO="${1:?Usage : $0 <url-du-depot> [branche]}"
BRANCH="${2:-main}"
DIR="$(mktemp -d -t bloodlink-clean-XXXXXX)"

echo "1/4 Clone neuf dans $DIR"
git clone --branch "$BRANCH" "$REPO" "$DIR"
cd "$DIR"

echo "2/4 cp .env.example .env"
cp .env.example .env

echo "3/4 docker compose up --build --wait"
if ! docker compose up --build -d --wait; then
  docker compose ps
  docker compose logs --tail=40
  echo "ECHEC : un service n'est pas devenu healthy" >&2
  exit 1
fi

echo "4/4 docker compose ps"
docker compose ps
echo "SUCCES : le projet démarre sur un clone neuf."
