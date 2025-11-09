#!/usr/bin/env bash
set -e

# перед запуском выполняем restart_docker.sh
# переходим в папку /tests-nbank
# далее bash infra/docker_compose/run-tests-with-docker-compose.sh api (или ui/all)

# === Настройки ===
IMAGE_NAME="nbank-tests"
TEST_PROFILE=${1:-api}   # Аргумент запуска: api, ui, all
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR="./test-output/$TIMESTAMP"

# === Подготовка окружения ===
echo ">>> Подготавливаем директории для результатов..."
mkdir -p "$TEST_OUTPUT_DIR/logs" "$TEST_OUTPUT_DIR/results" "$TEST_OUTPUT_DIR/report"

# === Функция для запуска тестов по профилю ===
run_tests() {
  local profile=$1
  echo ""
  echo "🚀 Запускаем тесты с профилем: $profile"
  echo "-----------------------------------------"

  # Строим Docker-образ из текущей директории
  docker build -t "$IMAGE_NAME" .

  # Базовые переменные окружения
  ENV_VARS="-e TEST_PROFILE=$profile \
            -e SERVER=http://backend:4111 \
            -e BASEURL=http://frontend:80"

  # Для UI-тестов добавляем только Selenoid
  if [[ "$profile" == "ui" ]]; then
    ENV_VARS="$ENV_VARS -e SELENOID_URI=http://selenoid:4444"
  fi

  # Запускаем контейнер и одновременно выводим лог в консоль и в файл
  docker run --rm \
    --network nbank-network \
    $ENV_VARS \
    -v "$TEST_OUTPUT_DIR/logs":/app/logs \
    -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$TEST_OUTPUT_DIR/report":/app/target/site \
    "$IMAGE_NAME" \
    bash -c "mvn test -P\$TEST_PROFILE | tee /app/logs/run.log"

  echo "✅ Тесты для профиля $profile завершены!"
  echo "-----------------------------------------"
}

# === Основная логика ===
if [[ "$TEST_PROFILE" == "all" ]]; then
  echo ">>> Запуск всех тестов: api и ui"
  run_tests "api"
  run_tests "ui"
else
  run_tests "$TEST_PROFILE"
fi

# === Вывод итогов ===
echo ""
echo "🏁 Все тесты завершены."
echo "📁 Логи: $TEST_OUTPUT_DIR/logs/run.log"
echo "📂 Результаты: $TEST_OUTPUT_DIR/results"
echo "📊 Отчёт: $TEST_OUTPUT_DIR/report"