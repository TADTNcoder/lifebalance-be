$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$target = "backups/postgres/lifebalance-$timestamp.dump"

docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > $target
Write-Host "Backup written to $target"
