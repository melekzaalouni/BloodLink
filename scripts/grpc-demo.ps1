# Démo gRPC de stock-service (partie 2 à 4 min de la soutenance).
# Prérequis : `docker compose up -d` lancé, Docker Desktop actif. Aucune installation de grpcurl nécessaire.
# Usage : .\scripts\grpc-demo.ps1
param([string]$Target = "host.docker.internal:9090")

$Service = "bloodlink.stock.v1.StockService"
$Image = "fullstorydev/grpcurl"

function Step($title) { Write-Host ""; Write-Host "=== $title ===" -ForegroundColor Cyan }

function Call($method, $json) {
    Write-Host "> $method $json" -ForegroundColor DarkGray
    $json | docker run --rm -i $Image -plaintext -d '@' $Target "$Service/$method"
}

Step "1. Services exposés (réflexion gRPC)"
docker run --rm $Image -plaintext $Target list

Step "2. Appel unaire : stock de O- (attendu : 4 unités, urgent)"
Call "GetStock" '{"blood_type":"O-"}'

Step "3. Server streaming : 8 groupes, 2 tours, 1 s d'intervalle"
Call "StreamStockLevels" '{"interval_seconds":1,"max_updates":2}'

Step "4. Erreur INVALID_ARGUMENT (groupe sanguin inconnu)"
Call "GetStock" '{"blood_type":"ZZ"}'

Step "5. Mise à jour : don de 10 unités de O- (4 -> 14, plus urgent)"
Call "UpdateStock" '{"blood_type":"O-","delta":10}'

Step "6. Erreur FAILED_PRECONDITION (retrait supérieur au stock)"
Call "UpdateStock" '{"blood_type":"O-","delta":-1000}'

Step "7. Remise de O- à 4 pour rejouer la démo"
Call "UpdateStock" '{"blood_type":"O-","delta":-10}'
