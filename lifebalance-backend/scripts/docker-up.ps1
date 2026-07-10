param(
    [switch] $WithTools
)

$profiles = @()
if ($WithTools) {
    $profiles = @("--profile", "messaging", "--profile", "cache", "--profile", "storage", "--profile", "monitoring")
}

docker compose @profiles up -d --build
