#!/usr/bin/env bash
set -euo pipefail

# run-tests-with-docker-compose.sh
# Запускает docker-compose (поднимает окружение), дожидается готовности сервисов,
# собирает образ с тестами и выполняет тесты в контейнере, подключённом к сети nbank-network.
#
# Использование:
#   bash ./run-tests-with-docker-compose.sh [TEST_PROFILE] [--keep-up]
# Пример:
#   bash ./run-tests-with-docker-compose.sh API
#   bash ./run-tests-with-docker-compose.sh UI --keep-up

# --- настройки по умолчанию ---
IMAGE_NAME="nbank-tests"
TEST_PROFILE="${1:-API}"
KEEP_UP=false

# если передали флаг --keep-up как второй аргумент
if [[ "${2:-}" == "--keep-up" ]] || [[ "${1:-}" == "--keep-up" && -n "${2:-}" ]]; then
  KEEP_UP=true
  # если флаг первый, возможно TEST_PROFILE не передан — поправим
  if [[ "${1:-}" == "--keep-up" ]]; then
    TEST_PROFILE="${2:-API}"
  fi
fi

# Директория скрипта (путь к проекту)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_OUTPUT_DIR="$SCRIPT_DIR/test-output/$TIMESTAMP"

# Порты (проверяем привязки на хосте; в WSL это будет localhost)
HOST_BACKEND="localhost"
PORT_BACKEND=4111
HOST_NGINX="localhost"
PORT_NGINX=80

# Функции
info() { echo -e "\n>>> $*"; }
err()  { echo -e "\n❌ $*" >&2; }

check_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    err "Команда '$1' не найдена. Установите её и повторите."
    exit 2
  fi
}

wait_for_port() {
  local host="$1"; local port="$2"; local timeout="${3:-60}"
  local start ts
  start=$(date +%s)
  info "Ожидаем доступности $host:$port (timeout ${timeout}s)..."
  while true; do
    # попытка открыть TCP сокет через /dev/tcp
    if bash -c "cat < /dev/tcp/${host}/${port}" >/dev/null 2>&1; then
      info "$host:$port доступен."
      return 0
    fi
    ts=$(($(date +%s) - start))
    if (( ts >= timeout )); then
      err "Таймаут: $host:$port всё ещё недоступен после ${timeout}s"
      return 1
    fi
    sleep 1
  done
}

# Проверки окружения
check_command docker
check_command docker-compose || true  # docker-compose v2 может быть через `docker compose`, не критично
# предпочитаем использовать `docker compose` (space). Проверим доступность.
if docker compose version >/dev/null 2>&1; then
  DC_CMD="docker compose"
elif docker-compose version >/dev/null 2>&1; then
  DC_CMD="docker-compose"
else
  err "Ни 'docker compose' ни 'docker-compose' не найдены/доступны."
  exit 2
fi

# Создаём директории вывода
mkdir -p "$TEST_OUTPUT_DIR/logs" "$TEST_OUTPUT_DIR/results" "$TEST_OUTPUT_DIR/report"

# --- Основной процесс ---
info "1) Останавливаем старое окружение (если есть)..."
# не фатальная команда — игнорируем ошибки
set +e
$DC_CMD down
set -e

info "2) Поднимаем тестовое окружение через docker compose (в фоне)..."
# используем --detach, не перезаписываем образы (images уже указаны в compose),
# но можно добавить --build-arg при необходимости
$DC_CMD up -d

# Ждём доступности основных сервисов по локальным портам (проверка через хост)
if ! wait_for_port "$HOST_BACKEND" "$PORT_BACKEND" 90; then
  err "backend не запустился корректно. Логи docker-compose:"
  $DC_CMD ps || true
  $DC_CMD logs --no-color --tail=200 backend || true
  exit 3
fi

if ! wait_for_port "$HOST_NGINX" "$PORT_NGINX" 90; then
  err "nginx/frontend не запустился корректно. Логи:"
  $DC_CMD ps || true
  $DC_CMD logs --no-color --tail=200 nginx || true
  exit 3
fi

info "3) Подготавливаем и собираем образ с тестами: $IMAGE_NAME"
# Собираем образ в контексте SCRIPT_DIR (предполагается, что Dockerfile там)
docker build -t "$IMAGE_NAME" "$SCRIPT_DIR"

info "4) Запускаем контейнер с тестами (он подключается к сети nbank-network)"
# контейнер подключаем к сети docker-compose (имя сети должно совпадать -> nbank-network)
# внутри сети сервисы доступны по именам: backend, nginx, selenoid и т.д.
docker run --rm \
  --network nbank-network \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e SERVER="http://backend:${PORT_BACKEND}" \
  -e BASEURL="http://nginx:${PORT_NGINX}" \
  "$IMAGE_NAME"

TEST_EXIT_CODE=$?

info "5) Сбор результатов"
info "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
info "Результаты тестов (surefire): $TEST_OUTPUT_DIR/results"
info "Отчёт (site): $TEST_OUTPUT_DIR/report"

if [[ $KEEP_UP = false ]]; then
  info "6) Завершаем docker-compose (очистка)..."
  $DC_CMD down --volumes --remove-orphans
else
  info "--keep-up задан: окружение оставлено поднятым для отладки."
fi

if [[ $TEST_EXIT_CODE -eq 0 ]]; then
  info "✅ Тесты завершены успешно (exit 0)."
else
  err "❌ Тесты завершились с кодом $TEST_EXIT_CODE."
fi

exit $TEST_EXIT_CODE