#!/usr/bin/env bash
set -euo pipefail

: "${SERVER_HOST:?Set SERVER_HOST, for example: export SERVER_HOST=123.60.10.88}"
: "${SERVER_USER:=root}"
: "${APP_DIR:=/opt/cao-erp}"
: "${WEB_DIR:=/var/www/cao-erp}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_JAR="$ROOT_DIR/ruoyi-admin/target/ruoyi-admin.jar"
FRONTEND_DIST="$ROOT_DIR/ruoyi-ui/dist"

cd "$ROOT_DIR"
mvn clean package -DskipTests

cd "$ROOT_DIR/ruoyi-ui"
npm install
npm run build:prod

ssh "$SERVER_USER@$SERVER_HOST" "mkdir -p '$APP_DIR' '$APP_DIR/config' '$WEB_DIR'"
scp "$BACKEND_JAR" "$SERVER_USER@$SERVER_HOST:$APP_DIR/ruoyi-admin.jar"
rsync -av --delete "$FRONTEND_DIST/" "$SERVER_USER@$SERVER_HOST:$WEB_DIR/"

echo "Uploaded backend jar and frontend dist."
echo "If this is the first deploy, also copy deploy/env/*.yml to $APP_DIR/config and install nginx/systemd configs."
echo "Then run on server: systemctl daemon-reload && systemctl restart cao-erp && systemctl reload nginx"
