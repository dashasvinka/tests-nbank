#!/usr/bin/env bash

# для запуска я использую в консоли bash ./run-tests.sh <TEST_PROFILE>

# настройка
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api} #аргумент запуска
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# сборка docker образ
echo ">>> Сборка тестов запущена"
docker build -t "$IMAGE_NAME" .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

echo ">>> Тесты запущены"
# запуск docker контейнера
docker run --rm \
 -v "$TEST_OUTPUT_DIR/logs":/app/logs \
 -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
 -v "$TEST_OUTPUT_DIR/report":/app/target/site \
 -e TEST_PROFILE="$TEST_PROFILE" \
 -e SERVER=http://host.docker.internal:4111 \
 -e BASEURL=http://host.docker.internal:3000 \
"$IMAGE_NAME"

# вывод итогов
echo ">>> Тесты завершены"
echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Отчет: $TEST_OUTPUT_DIR/report"