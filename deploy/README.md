# Cao ERP deployment

This project is a RuoYi Vue + Spring Boot application.

Production request flow:

```text
https://your-domain.com
  -> Nginx static files: /var/www/cao-erp

https://your-domain.com/prod-api/system/user/list
  -> Nginx
  -> http://127.0.0.1:8080/system/user/list
  -> ruoyi-admin.jar
```

## 1. DNS

In your domain console, add A records:

```text
@    -> your server public IP
www  -> your server public IP
```

After a few minutes, verify:

```bash
nslookup your-domain.com
nslookup www.your-domain.com
```

## 2. Server ports

Open these ports in the cloud security group:

```text
22   SSH
80   HTTP
443  HTTPS
```

Do not expose `8080` unless you are temporarily debugging.

## 3. First-time server setup

Create the app user and directories:

```bash
useradd -r -s /sbin/nologin caoerp || true
mkdir -p /opt/cao-erp/config /opt/cao-erp/uploadPath /var/www/cao-erp
chown -R caoerp:caoerp /opt/cao-erp
```

Install Java 17, MySQL, Redis and Nginx according to your server OS.

Copy config templates:

```bash
cp deploy/env/application-prod.yml /opt/cao-erp/config/
cp deploy/env/application-druid-prod.yml /opt/cao-erp/config/
cp deploy/env/cao-erp.env.example /opt/cao-erp/cao-erp.env
```

Edit `/opt/cao-erp/cao-erp.env` and fill in MySQL, Redis and JWT values.

## 4. Nginx

Copy `deploy/nginx/cao-erp.conf` to the server:

```bash
cp deploy/nginx/cao-erp.conf /etc/nginx/conf.d/cao-erp.conf
```

Edit it and replace:

```text
example.com www.example.com
```

with your real domain.

Then test and reload:

```bash
nginx -t
systemctl reload nginx
```

## 5. systemd

Copy `deploy/systemd/cao-erp.service`:

```bash
cp deploy/systemd/cao-erp.service /etc/systemd/system/cao-erp.service
systemctl daemon-reload
systemctl enable cao-erp
```

## 6. Database

Create database and user:

```sql
CREATE DATABASE `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'ruoyi'@'127.0.0.1' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON `ry-vue`.* TO 'ruoyi'@'127.0.0.1';
FLUSH PRIVILEGES;
```

Import SQL files from the `sql/` directory according to your current data state.

## 7. Deploy from local machine

From the project root:

```bash
export SERVER_HOST=your.server.ip
export SERVER_USER=root
bash deploy/scripts/deploy.sh
```

Then on the server:

```bash
chown -R caoerp:caoerp /opt/cao-erp
systemctl restart cao-erp
systemctl status cao-erp
journalctl -u cao-erp -f
```

## 8. HTTPS

After DNS points to the server and Nginx works on port 80:

```bash
certbot --nginx -d your-domain.com -d www.your-domain.com
```

Then verify:

```bash
curl -I https://your-domain.com
curl -I https://your-domain.com/prod-api/captchaImage
```

## 9. Common checks

Backend local check on server:

```bash
curl http://127.0.0.1:8080/captchaImage
```

Nginx public check:

```bash
curl http://your-domain.com/prod-api/captchaImage
```

If the first works but the second fails, check Nginx.
If the first fails, check `journalctl -u cao-erp -f`.
