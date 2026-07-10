param(
    [string] $Service = ""
)

if ([string]::IsNullOrWhiteSpace($Service)) {
    docker compose build --no-cache
} else {
    docker compose build --no-cache $Service
}
