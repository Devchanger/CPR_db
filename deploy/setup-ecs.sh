#!/usr/bin/env bash
# CPR DB 部署初始化脚本 (Ubuntu 24/26.04)
# 用法: sudo bash setup-ecs.sh
set -euo pipefail

DEPLOY_DIR=/opt/cpr-db

echo "[1/6] 安装依赖 (openjdk-17-jdk, mysql-server) ..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y openjdk-17-jdk mysql-server

echo "[2/6] 启动并启用 MySQL ..."
systemctl enable --now mysql

echo "[3/6] 初始化数据库 (建库/建账号/赋权) ..."
# Ubuntu 的 MySQL 初始 root 走 auth_socket，用 sudo mysql 直连
mysql < "${DEPLOY_DIR}/init.sql"

echo "[4/6] 创建专用系统用户 cpr 并赋权目录 ..."
if ! id cpr >/dev/null 2>&1; then
  useradd --system --no-create-home --shell /usr/sbin/nologin cpr
fi
chown -R cpr:cpr "${DEPLOY_DIR}"
chmod 600 "${DEPLOY_DIR}/.env"
chmod 644 "${DEPLOY_DIR}/application.properties" "${DEPLOY_DIR}/cpr-db.service" "${DEPLOY_DIR}/init.sql"

echo "[5/6] 注册 systemd 服务 ..."
cp "${DEPLOY_DIR}/cpr-db.service" /etc/systemd/system/cpr-db.service
systemctl daemon-reload
systemctl enable --now cpr-db

echo "[6/6] 防火墙：按 ECS 规范不在此脚本内启用 ufw（default-deny 会阻断未声明端口）"
echo "       请在脚本外手动放行 22/8080（3306 仅内网，无需对外）。"

echo "=== 部署完成! 服务状态: ==="
systemctl status cpr-db --no-pager || true
