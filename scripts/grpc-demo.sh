#!/usr/bin/env bash
# Démo gRPC de stock-service. Usage : ./scripts/grpc-demo.sh
# Nécessite grpcurl installé (grpcurl -version) et `docker compose up -d` lancé.
set -u
TARGET="${1:-localhost:9090}"
SVC="bloodlink.stock.v1.StockService"

step() { printf '\n=== %s ===\n' "$1"; }
call() { echo "> $1 $2"; grpcurl -plaintext -d "$2" "$TARGET" "$SVC/$1"; }

step "1. Services exposés"
grpcurl -plaintext "$TARGET" list
step "2. Appel unaire : O- (attendu : 4 unités, urgent)"
call GetStock '{"blood_type":"O-"}'
step "3. Server streaming (8 groupes x 2 tours)"
call StreamStockLevels '{"interval_seconds":1,"max_updates":2}'
step "4. INVALID_ARGUMENT"
call GetStock '{"blood_type":"ZZ"}'
step "5. Don de 10 unités de O-"
call UpdateStock '{"blood_type":"O-","delta":10}'
step "6. FAILED_PRECONDITION"
call UpdateStock '{"blood_type":"O-","delta":-1000}'
step "7. Remise de O- à 4"
call UpdateStock '{"blood_type":"O-","delta":-10}'
