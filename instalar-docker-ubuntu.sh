#!/usr/bin/env bash
set -euo pipefail
[[ $EUID -eq 0 ]] || { echo "Ejecuta con sudo."; exit 1; }
. /etc/os-release
[[ ${ID:-} == ubuntu && ${VERSION_ID:-} == 24.04 ]] || {
    echo "Este instalador es para Ubuntu Server 24.04 LTS."
    exit 1
}
if command -v docker >/dev/null; then
    if docker compose version >/dev/null 2>&1; then
        systemctl enable --now docker
        echo "Docker y Compose ya están disponibles."
    else
        echo "Docker ya existe pero falta Compose v2. Instala el plugin compatible con tu instalación."
        exit 1
    fi
    exit 0
fi
apt-get update
apt-get install -y ca-certificates curl openssl unzip
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
cat > /etc/apt/sources.list.d/docker.sources <<SOURCES
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${UBUNTU_CODENAME:-$VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
SOURCES
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker compose version
