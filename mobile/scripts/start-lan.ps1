$ErrorActionPreference = "Stop"

function Get-PreferredLanIp {
  $candidates = @()
  $output = ipconfig
  foreach ($line in $output) {
    if ($line -match "IPv4 Address[^\:]*:\s*(\d+\.\d+\.\d+\.\d+)") {
      $ip = $Matches[1]
      if ($ip -notlike "127.*" -and $ip -notlike "169.254.*") {
        $candidates += $ip
      }
    }
  }
  if ($candidates.Count -eq 0) {
    throw "Could not detect a LAN IPv4 address from ipconfig output."
  }
  return $candidates[0]
}

function Update-ApiEnvFile {
  param(
    [string]$MobileRoot,
    [string]$LanIp
  )

  $envPath = Join-Path $MobileRoot ".env"
  $desired = "EXPO_PUBLIC_API_URL=http://${LanIp}:8080"

  if (-not (Test-Path $envPath)) {
    Set-Content -Path $envPath -Value $desired -Encoding utf8
    return $envPath
  }

  $lines = Get-Content -Path $envPath
  $found = $false
  $updated = New-Object System.Collections.Generic.List[string]

  foreach ($line in $lines) {
    if ($line -match "^\s*EXPO_PUBLIC_API_URL\s*=") {
      $updated.Add($desired)
      $found = $true
    } else {
      $updated.Add($line)
    }
  }

  if (-not $found) {
    if ($updated.Count -gt 0 -and $updated[$updated.Count - 1].Trim().Length -gt 0) {
      $updated.Add("")
    }
    $updated.Add($desired)
  }

  Set-Content -Path $envPath -Value $updated -Encoding utf8
  return $envPath
}

$lanIp = Get-PreferredLanIp
$metroPort = if ($env:EXPO_METRO_PORT -and $env:EXPO_METRO_PORT.Trim().Length -gt 0) { $env:EXPO_METRO_PORT.Trim() } else { "8082" }
$mobileRoot = Split-Path -Parent $PSScriptRoot
$envFile = Update-ApiEnvFile -MobileRoot $mobileRoot -LanIp $lanIp

# Keep Expo/ngrok away from intentionally broken proxy defaults in this environment.
$env:HTTP_PROXY = ""
$env:HTTPS_PROXY = ""
$env:ALL_PROXY = ""
$env:GIT_HTTP_PROXY = ""
$env:GIT_HTTPS_PROXY = ""

$env:REACT_NATIVE_PACKAGER_HOSTNAME = $lanIp
$env:EXPO_PACKAGER_PROXY_URL = "http://${lanIp}:${metroPort}"

Write-Host "Starting Expo on LAN host: $lanIp"
Write-Host "Metro port: $metroPort"
Write-Host "Updated API URL in: $envFile"
Write-Host "Expo Go URL: exp://${lanIp}:${metroPort}"

npx expo start --go --lan --clear --port $metroPort
