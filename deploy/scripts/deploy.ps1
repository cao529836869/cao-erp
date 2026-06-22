param(
    [Parameter(Mandatory = $true)]
    [string]$ServerHost,

    [string]$ServerUser = "root",
    [string]$AppDir = "/opt/cao-erp",
    [string]$WebDir = "/var/www/cao-erp"
)

$ErrorActionPreference = "Stop"

$RootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
$BackendJar = Join-Path $RootDir "ruoyi-admin/target/ruoyi-admin.jar"
$FrontendDist = Join-Path $RootDir "ruoyi-ui/dist"

Set-Location $RootDir
mvn clean package -DskipTests

Set-Location (Join-Path $RootDir "ruoyi-ui")
npm install
npm run build:prod

ssh "$ServerUser@$ServerHost" "mkdir -p '$AppDir' '$AppDir/config' '$WebDir'"
scp "$BackendJar" "$ServerUser@$ServerHost`:$AppDir/ruoyi-admin.jar"
scp -r "$FrontendDist/*" "$ServerUser@$ServerHost`:$WebDir/"

Write-Host "Uploaded backend jar and frontend dist."
Write-Host "First deploy still needs nginx/systemd/env files installed on the server. See deploy/README.md."
