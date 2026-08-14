#!/usr/bin/env python3
"""CPR DB — 一键部署到 AgerEnd ECS (123.57.30.132)"""

import paramiko, time, sys, os

HOST = "123.57.30.132"
PORT = 22
USER = "root"
SSH_PASSWORD_ENV = "CPR_ECS_SSH_PASSWORD"
DEPLOY_FILES = os.path.dirname(os.path.abspath(__file__))  # deploy/ dir
SRC_REPO = "https://github.com/Devchanger/CPR_db.git"
APP_DIR = "/opt/cpr-db"
SRC_DIR = "/opt/cpr-db-src"


def resolve_ssh_password() -> str:
    password = os.environ.get(SSH_PASSWORD_ENV)
    if not password:
        print(f"[ABORT] 未设置环境变量 {SSH_PASSWORD_ENV}（服务器 SSH 密码），拒绝使用硬编码凭据。")
        print(f"请先导出：export {SSH_PASSWORD_ENV}='...'（或用 SSH 密钥方案替换）。")
        sys.exit(1)
    return password


def run(ssh: paramiko.SSHClient, cmd: str, desc: str = ""):
    label = desc or cmd[:60]
    print(f"\n>>> {label}")
    _, stdout, stderr = ssh.exec_command(cmd, timeout=600)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    exit_code = stdout.channel.recv_exit_status()
    # print both
    if out.strip():
        print(out.strip())
    if err.strip():
        print("[STDERR]", err.strip()[:300])
    if exit_code != 0:
        print(f"[EXIT={exit_code}] ABORT on: {label}")
        sys.exit(1)
    return out, err


def main():
    password = resolve_ssh_password()
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    print(f"🔗 连接 {HOST} ...")
    ssh.connect(HOST, PORT, USER, password, timeout=15, look_for_keys=False, allow_agent=False)

    # ── Step 1: 系统依赖 ──
    run(ssh,
        "export DEBIAN_FRONTEND=noninteractive && "
        "apt-get update -qq && "
        "apt-get install -y -qq openjdk-17-jdk mysql-server maven ufw",
        "安装 openjdk17 + mysql + maven + ufw")

    # ── Step 2: MySQL 启动 ──
    run(ssh, "systemctl enable --now mysql", "启动 MySQL")

    # ── Step 3: Clone 源码 ──
    run(ssh, f"rm -rf {SRC_DIR} && git clone {SRC_REPO} {SRC_DIR}", "Git clone 源码")

    # ── Step 4: Maven 构建 ──
    run(ssh, f"cd {SRC_DIR} && mvn -DskipTests clean package -q",
        "Maven 构建 (可能1-2分钟，下依赖)")

    # ── Step 5: 创建目标目录，复制 jar ──
    run(ssh, f"mkdir -p {APP_DIR} && cp {SRC_DIR}/target/*.jar {APP_DIR}/app.jar",
        "复制 jar 到 /opt/cpr-db")

    # ── Step 6: SFTP 上传部署配置文件 ──
    print("\n>>> SFTP 上传配置文件 ...")
    sftp = ssh.open_sftp()
    for fname in [".env", "application.properties", "init.sql", "cpr-db.service"]:
        local = os.path.join(DEPLOY_FILES, fname)
        remote = f"{APP_DIR}/{fname}"
        sftp.put(localpath=local, remotepath=remote)
        print(f"  ✓ {fname}")
    sftp.put(local_path=os.path.join(DEPLOY_FILES, "setup-ecs.sh"), remotepath=f"{APP_DIR}/setup-ecs.sh")
    sftp.close()

    # ── Step 7: 初始化数据库 ──
    run(ssh, f"mysql < {APP_DIR}/init.sql", "初始化 MySQL 数据库")

    # ── Step 8: 创建用户 & 权限 ──
    run(ssh, "id cpr >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin cpr",
        "创建 cpr 系统用户")
    run(ssh, f"chown -R cpr:cpr {APP_DIR} && chmod 600 {APP_DIR}/.env && "
             f"chmod 644 {APP_DIR}/application.properties {APP_DIR}/cpr-db.service",
        "设置文件权限")

    # ── Step 9: 注册服务 ──
    run(ssh, f"cp {APP_DIR}/cpr-db.service /etc/systemd/system/cpr-db.service && systemctl daemon-reload",
        "注册 systemd 服务")

    # ── Step 10: 防火墙 ──
    run(ssh, "ufw allow 22/tcp && ufw allow 8080/tcp && ufw --force enable || true",
        "防火墙放行 22 & 8080")

    # ── Step 11: 启动 ──
    time.sleep(2)
    run(ssh, "systemctl enable --now cpr-db", "启动 cpr-db 服务")

    # ── Step 12: 状态 ──
    print("\n── 部署完成，服务状态 ──")
    run(ssh, "systemctl status cpr-db --no-pager -l || true", "服务状态")
    run(ssh, "sleep 3 && journalctl -u cpr-db --no-pager -n 15", "最近日志")
    run(ssh, "curl -s -o /dev/null -w 'HTTP %{http_code}' http://127.0.0.1:8080/actuator/health 2>/dev/null || echo '健康检查端点未暴露'",
        "健康检查")

    ssh.close()
    print("\n✅ 全部完成! 访问 http://123.57.30.132:8080")


if __name__ == "__main__":
    main()
