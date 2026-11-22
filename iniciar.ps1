$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Construyendo la base, el API y el frontend..."
docker compose up --build -d
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Esperando a que la aplicacion responda en http://localhost:8088 ..."
$lista = $false
for ($i = 0; $i -lt 40; $i++) {
    try {
        $respuesta = Invoke-WebRequest -Uri "http://localhost:8088/api/cuentas?tamano=1" -UseBasicParsing -TimeoutSec 5
        if ($respuesta.StatusCode -eq 200) {
            $lista = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 3
    }
}

Start-Process "http://localhost:8088"
if ($lista) {
    Write-Host "Listo. Interfaz: http://localhost:8088"
    Write-Host "Swagger: http://localhost:8088/q/swagger-ui"
} else {
    Write-Host "El entorno sigue arrancando. La interfaz ya se abrio en http://localhost:8088"
}
