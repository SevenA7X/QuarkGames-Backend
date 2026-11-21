#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

[[ -r .env ]] || { echo "Ejecuta primero ./configurar.sh ssh o ./configurar.sh https ..."; exit 1; }
docker compose version >/dev/null
mode=$(sed -n 's/^DEPLOY_MODE=//p' .env)
compose=(docker compose --env-file .env -f compose.yaml)
case "$mode" in
    ssh) ;;
    https) compose+=(-f compose.https.yaml) ;;
    *) echo "DEPLOY_MODE debe ser ssh o https"; exit 2 ;;
esac

case "${1:-up}" in
    up)
        "${compose[@]}" config --quiet
        COMPOSE_PARALLEL_LIMIT=1 "${compose[@]}" build
        "${compose[@]}" up -d --wait --wait-timeout 300
        "${compose[@]}" ps
        echo 'Comprobación HTTP: ./desplegar.sh check'
        ;;
    ps) "${compose[@]}" ps ;;
    logs) shift; "${compose[@]}" logs --tail=100 "$@" ;;
    down) "${compose[@]}" down ;;
    check)
        url=$(sed -n 's/^PUBLIC_URL=//p' .env)
        origin=$(sed -n 's/^FRONTEND_ORIGINS=//p' .env)
        curl -fsS --max-time 10 "$url/actuator/health"
        printf '\n'
        curl -fsS --max-time 10 "$url/api/v1/juegos" -o /dev/null
        printf 'Catálogo por Gateway: OK\n'
        headers=$(curl -fsS --max-time 10 -D - -o /dev/null -X OPTIONS \
            -H "Origin: $origin" -H 'Access-Control-Request-Method: POST' \
            -H 'Access-Control-Request-Headers: authorization,content-type,x-carrito-id' \
            "$url/api/v1/juegos")
        if ! printf '%s\n' "$headers" | tr -d '\r' | grep -iFx "Access-Control-Allow-Origin: $origin" >/dev/null; then
            echo "Preflight sin el encabezado esperado para $origin"
            exit 1
        fi
        echo "Preflight CORS: OK ($origin)"
        ;;
    *) echo "Uso: ./desplegar.sh [up|ps|logs [servicio]|down|check]"; exit 2 ;;
esac