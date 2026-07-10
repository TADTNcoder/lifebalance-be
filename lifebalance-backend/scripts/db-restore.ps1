param(
    [Parameter(Mandatory = $true)]
    [string] $BackupFile
)

if (-not (Test-Path $BackupFile)) {
    throw "Backup file not found: $BackupFile"
}

$containerPath = "/tmp/restore.dump"
docker compose cp $BackupFile "postgres:$containerPath"
docker compose exec -T postgres sh -c "pg_restore -U `"`$POSTGRES_USER`" -d `"`$POSTGRES_DB`" --clean --if-exists $containerPath"
docker compose exec -T postgres rm -f $containerPath
