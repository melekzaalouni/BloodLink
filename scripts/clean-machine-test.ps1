# Test « machine propre » : clone le dépôt dans un dossier neuf, sans rien copier à la main,
# puis lance tout avec Docker Compose.
# Usage : .\scripts\clean-machine-test.ps1 -RepoUrl https://github.com/<compte>/BloodLink.git [-Branch main] [-Cleanup]
# IMPORTANT : arrête d'abord ta pile de travail (docker compose down) pour libérer les ports 9090, 8080...
param(
    [Parameter(Mandatory = $true)][string]$RepoUrl,
    [string]$Branch = "main",
    [switch]$Cleanup
)

function Fail($msg) { Write-Host "ECHEC : $msg" -ForegroundColor Red; exit 1 }

$dir = Join-Path $env:TEMP ("bloodlink-clean-" + (Get-Date -Format "yyyyMMdd-HHmmss"))

Write-Host "1/5 Clone neuf dans $dir" -ForegroundColor Cyan
git clone --branch $Branch $RepoUrl $dir
if ($LASTEXITCODE -ne 0) { Fail "git clone" }
Set-Location $dir

Write-Host "2/5 cp .env.example .env" -ForegroundColor Cyan
Copy-Item .env.example .env

Write-Host "3/5 docker compose up --build --wait (attend que tout soit healthy)" -ForegroundColor Cyan
docker compose up --build -d --wait
if ($LASTEXITCODE -ne 0) {
    docker compose ps
    docker compose logs --tail=40
    Fail "un service n'est pas devenu healthy"
}

Write-Host "4/5 docker compose ps" -ForegroundColor Cyan
docker compose ps

Write-Host "5/5 Vérification gRPC : O- doit avoir 4 unités" -ForegroundColor Cyan
$out = '{"blood_type":"O-"}' | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 bloodlink.stock.v1.StockService/GetStock
$out
if (($out | Out-String) -notmatch '"units": 4') { Fail "réponse gRPC inattendue" }

Write-Host ""
Write-Host "SUCCES : le projet démarre sur un clone neuf." -ForegroundColor Green

if ($Cleanup) {
    docker compose down -v
    Set-Location $env:TEMP
    Remove-Item -Recurse -Force $dir
}
