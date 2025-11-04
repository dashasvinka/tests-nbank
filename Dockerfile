# базовый докер образ
FROM maven:3.9.9-eclipse-temurin-21

# дефолтные значения аргументов
ARG TEST_PROFILE=API
ARG SERVER=http://localhost:4111
ARG BASEURL=http://localhost:3000

# переменные окружения для контейнера
# переменная внутри контейнера = переменной снаружи контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV SERVER=${SERVER}
ENV BASEURL=${BASEURL}

# установка рабочей директории - папки app
WORKDIR /app

# копирование pom.xml в установленную директорию
COPY pom.xml .

# установка и кеширование зависимостей
# RUN mvn dependency:go-offline
RUN mvn -B dependency:go-offline \
    -Dhttp.socket.timeout=60000 \
    -Dhttp.connection.timeout=60000 \
    -Dhttps.protocols=TLSv1.2,TLSv1.3

# копирование всего сушествующего проекта в установленную директорию
COPY . .

# установка пользователя, под которым осуществляется запуск тестов
USER root

# запуск тестов и запись лога в отдельных файл
CMD /bin/bash -c " \
    mkdir -p /app/logs; \
    { \
    echo '>>> Running tests with profiles: ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
    } > /app/logs/run.log 2>&1"



