[CmdletBinding()]
param(
    [string]$EnvFile = '.env',
    [string]$OutputDirectory = 'openapi/generated'
)

$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path -Parent $PSScriptRoot
$outputRoot = Join-Path $backendRoot $OutputDirectory
$services = @(
    'identity-service',
    'task-service',
    'timeline-service',
    'resource-capital-service',
    'finance-service',
    'notification-service',
    'analytics-service',
    'ai-service'
)
$composeArgs = @(
    'compose',
    '--env-file', $EnvFile,
    '-f', 'compose.yaml',
    '-f', 'compose.prod.yaml',
    '-f', 'compose.staging.yaml'
)

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
$manifest = @()

Push-Location $backendRoot
try {
    foreach ($service in $services) {
        Write-Host "Exporting OpenAPI for $service..."
        $json = & docker @composeArgs exec -T $service curl -fsS http://localhost:8080/v3/api-docs
        if ($LASTEXITCODE -ne 0 -or -not $json) {
            throw "Unable to export OpenAPI from $service."
        }

        $document = $json | ConvertFrom-Json
        $pathCount = @($document.paths.PSObject.Properties).Count
        if ($pathCount -lt 1) {
            throw "OpenAPI document for $service does not contain any paths."
        }

        $outputPath = Join-Path $outputRoot "$service.json"
        $document | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath $outputPath -Encoding utf8
        $manifest += [pscustomobject]@{
            service = $service
            paths = $pathCount
            file = "$service.json"
        }
    }
}
finally {
    Pop-Location
}

$manifest | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $outputRoot 'manifest.json') -Encoding utf8
$manifest | Format-Table -AutoSize
Write-Host "OpenAPI export completed: $outputRoot"
