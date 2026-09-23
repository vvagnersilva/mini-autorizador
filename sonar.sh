#!/usr/bin/env bash
#
# Gera o relatorio do SonarQube do projeto com um unico comando:
#
#   ./sonar.sh
#
# 1. Sobe o SonarQube local em container (cria na primeira vez; depois so inicia)
# 2. Aguarda o servidor ficar disponivel
# 3. Define a senha do usuario admin (na primeira execucao)
# 4. Gera um token de analise
# 5. Roda os testes (com cobertura do JaCoCo) e envia a analise
#
# Ao final, basta acessar a URL exibida com o usuario admin e a senha abaixo.
#
set -euo pipefail

SONAR_CONTAINER="${SONAR_CONTAINER:-sonarqube}"
SONAR_PORT="${SONAR_PORT:-9000}"
SONAR_IMAGE="docker.io/library/sonarqube:community"
SONAR_URL="http://localhost:${SONAR_PORT}"
SONAR_USER="admin"
SONAR_PASSWORD="Sonar@Local2026"
SONAR_PROJECT="mini-autorizador"
TOKEN_NAME="mini-autorizador-sonar-sh"

cd "$(dirname "$0")"

log() { printf '\n==> %s\n' "$*"; }

# ---------------------------------------------------------------- 1. container
if docker container inspect "$SONAR_CONTAINER" >/dev/null 2>&1; then
    log "Iniciando o container '$SONAR_CONTAINER' (ja existente)"
    docker start "$SONAR_CONTAINER" >/dev/null
else
    log "Criando o container '$SONAR_CONTAINER' (a primeira vez baixa a imagem e pode demorar)"
    docker run -d --name "$SONAR_CONTAINER" -p "${SONAR_PORT}:9000" "$SONAR_IMAGE" >/dev/null
fi

# ---------------------------------------------------------------- 2. aguarda UP
log "Aguardando o SonarQube ficar disponivel em $SONAR_URL"
for _ in $(seq 1 120); do
    curl -s "$SONAR_URL/api/system/status" | grep -q '"status":"UP"' && break
    sleep 5
done
curl -s "$SONAR_URL/api/system/status" | grep -q '"status":"UP"' \
    || { echo "SonarQube nao ficou disponivel. Veja: docker logs $SONAR_CONTAINER" >&2; exit 1; }

# ---------------------------------------------------------------- 3. senha do admin
autentica() {
    curl -s -u "$SONAR_USER:$1" "$SONAR_URL/api/authentication/validate" | grep -q '"valid":true'
}

if autentica "admin"; then
    log "Definindo a senha do usuario $SONAR_USER"
    curl -sf -u "$SONAR_USER:admin" -X POST "$SONAR_URL/api/users/change_password" \
        --data-urlencode "login=$SONAR_USER" \
        --data-urlencode "previousPassword=admin" \
        --data-urlencode "password=$SONAR_PASSWORD" >/dev/null
fi
autentica "$SONAR_PASSWORD" \
    || { echo "Nao foi possivel autenticar como $SONAR_USER com a senha padrao do projeto." >&2; exit 1; }

# ---------------------------------------------------------------- 4. token
log "Gerando token de analise"
curl -s -u "$SONAR_USER:$SONAR_PASSWORD" -X POST "$SONAR_URL/api/user_tokens/revoke" \
    -d "name=$TOKEN_NAME" >/dev/null || true
SONAR_TOKEN="$(curl -sf -u "$SONAR_USER:$SONAR_PASSWORD" -X POST "$SONAR_URL/api/user_tokens/generate" \
    -d "name=$TOKEN_NAME" -d "type=GLOBAL_ANALYSIS_TOKEN" \
    | sed -E 's/.*"token":"([^"]+)".*/\1/')"
export SONAR_TOKEN

# ---------------------------------------------------------------- 5. testes + analise
# Com Podman, aponta o Testcontainers para o socket, para que os testes E2E tambem rodem
# (e entrem na cobertura). Sem Docker/Podman acessivel, os E2E sao apenas pulados.
PODMAN_SOCKET="/run/user/$(id -u)/podman/podman.sock"
if [ -z "${DOCKER_HOST:-}" ] && [ -S "$PODMAN_SOCKET" ]; then
    export DOCKER_HOST="unix://$PODMAN_SOCKET"
    export TESTCONTAINERS_RYUK_DISABLED=true
fi

log "Executando os testes e enviando a analise"
mvn -B clean verify sonar:sonar -Dsonar.host.url="$SONAR_URL"

log "Aguardando o processamento da analise"
for _ in $(seq 1 60); do
    curl -s -u "$SONAR_USER:$SONAR_PASSWORD" "$SONAR_URL/api/ce/component?component=$SONAR_PROJECT" \
        | grep -q '"queue":\[\]' && break
    sleep 2
done

cat <<EOF

================================================================
 Relatorio do SonarQube pronto

   URL:     $SONAR_URL/dashboard?id=$SONAR_PROJECT
   Usuario: $SONAR_USER
   Senha:   $SONAR_PASSWORD

 Para parar o SonarQube: docker stop $SONAR_CONTAINER
================================================================
EOF
