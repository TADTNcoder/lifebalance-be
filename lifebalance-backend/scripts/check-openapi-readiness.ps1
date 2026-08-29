[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path -Parent $PSScriptRoot
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

$results = foreach ($service in $services) {
    $serviceRoot = Join-Path $backendRoot $service
    $pomPath = Join-Path $serviceRoot 'pom.xml'
    $productionConfigPath = Join-Path $serviceRoot 'src/main/resources/application-prod.yaml'
    $controllerFiles = Get-ChildItem (Join-Path $serviceRoot 'src/main/java') -Recurse -File -Filter '*.java' |
        Where-Object { Select-String -LiteralPath $_.FullName -Pattern '@RestController' -Quiet }
    $pom = Get-Content -Raw -LiteralPath $pomPath
    $productionConfig = Get-Content -Raw -LiteralPath $productionConfigPath

    [pscustomobject]@{
        Service = $service
        Controllers = @($controllerFiles).Count
        Springdoc = $pom.Contains('springdoc-openapi-starter-webmvc-ui')
        ProductionDocsDisabled = $productionConfig -match '(?ms)springdoc:.*?api-docs:.*?enabled:\s*false'
    }
}

$results | Format-Table -AutoSize

$violations = @($results | Where-Object {
    $_.Controllers -lt 1 -or -not $_.Springdoc -or -not $_.ProductionDocsDisabled
})

if ($violations.Count -gt 0) {
    throw "OpenAPI readiness failed for: $($violations.Service -join ', ')"
}

Write-Host "OpenAPI readiness passed for $($results.Count) business services."
