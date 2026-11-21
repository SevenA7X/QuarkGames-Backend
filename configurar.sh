#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

uso() {
    echo "Uso: ./configurar.sh ssh"
    echo "  o: ./configurar.sh https api.midominio.cl https://app.midominio.cl"
    exit 2
}

[[ ! -e .env && ! -L .env ]] || { echo ".env ya existe. Se conservó para no perder acceso a los volúmenes."; exit 1; }
command -v openssl >/dev/null || { echo "Instala openssl antes de continuar."; exit 1; }

mode=${1:-}
domain='^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$'
case "$mode" in
    ssh)
        [[ $# -eq 1 ]] || uso
        api_host=
        public_url=http://localhost:8080
        origins=http://localhost:5173
        ;;
    https)
        [[ $# -eq 3 && $2 =~ $domain && $2 =~ [a-z] && $3 == https://* ]] || uso
        front_host=${3#https://}
        [[ $front_host =~ $domain && $front_host =~ [a-z] ]] || uso
        api_host=$2
        public_url=https://$api_host
        origins=$3
        ;;
    *) uso ;;
esac

umask 077
tmp=$(mktemp .env.tmp.XXXXXX)
trap 'rm -f -- "$tmp"' EXIT
{
    printf 'DEPLOY_MODE=%s\nAPI_HOST=%s\nPUBLIC_URL=%s\nFRONTEND_ORIGINS=%s\n' \
        "$mode" "$api_host" "$public_url" "$origins"
    printf 'ADMIN_PASSWORD=Qg-%s\n' "$(openssl rand -hex 16)"
    # Clave HMAC del JWT en Base64 (384 bits). Auth firma y Catálogo valida con la misma.
    printf 'JWT_SECRET=%s\n' "$(openssl rand -base64 48 | tr -d '\n')"
    printf 'INTERNAL_API_KEY=%s\n' "$(openssl rand -hex 32)"
    for service in AUTH CATALOGO CARRITO; do
        printf 'DB_%s_PASSWORD=%s\n' "$service" "$(openssl rand -hex 24)"
        printf 'DB_%s_ROOT_PASSWORD=%s\n' "$service" "$(openssl rand -hex 24)"
    done
} > "$tmp"
# El enlace falla si alguien creó .env entre la comprobación y este paso.
ln -- "$tmp" .env
echo "Configuración creada. Siguiente paso: ./desplegar.sh"