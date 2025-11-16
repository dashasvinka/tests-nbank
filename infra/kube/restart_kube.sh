#!/bin/bash

# ШАГ 1: поднятие сервисов приложения

# Запустили локальный Kubernetes-кластер с помощью minikube, используя Docker как драйвер
# (кластер будет запущен внутри докер контейнера)
minikube start --driver=docker

# Создали ConfigMap с именем selenoid-config, файл будет доступен под ключом browsers.json
kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

# Устанавливаем Helm чарт с именем релиза nbank, беря шаблоны из ./nbank-chart
# Это создаст все ресурсв, описанные в шаблонах Helm (Deployment, Service)
helm install nbank ./nbank-chart

# Все сервисы в namespace=default
kubectl get svc

# Все поды в namespace=default
kubectl get pods

# Логи конкретного сервиса
kubectl logs deployment/backend

# Проброс портов на локальную машину
kubectl port-forward svc/frontend 3000:80 #  > /dev/null 2>&1 & (проброс порта в фоновом режиме)
kubectl port-forward svc/backend 4111:4111
kubectl port-forward svc/selenoid 4444:4444
kubectl port-forward svc/selenoid-ui 8080:8080

# ШАГ 2: поднятие сервисов мониторинга
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

# или:
#helm repo add prometheus-community https://prometheus-community.github.io/helm-charts; $null = $?
#helm repo add elastic https://helm.elastic.co; $null = $?
#helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f monitoring-values.yaml

# Пробрасываем порт к прометеусу и графане
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090 # > /dev/null 2>&1
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80

# Создаем секреты для авторизации на бекенде
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring

# Применяем yaml с настройкой SpringMonitoring за бекендом
kubectl apply -f spring-monitoring.yaml

# ШАГ 3: Логирование (Elasticsearch + Kibana + Filebeat)
#НЕ ПОДНЯЛОСЬ
# Создаем отдельный namespace для логирования
#kubectl create namespace logging || true
#kubectl create namespace logging; $null = $?

#Invoke-WebRequest "https://helm.elastic.co/helm/elasticsearch/elasticsearch-8.5.1.tgz" -OutFile .\elasticsearch-8.5.1.tgz -Verbose

# Скачиваем чарт Elasticsearch версии 8.5.1
#helm install elasticsearch .\elasticsearch-8.5.1.tgz -n logging --create-namespace `
#  --set replicas=1 --set minimumMasterNodes=1 --set persistence.enabled=false `
#  --set antiAffinity="soft" --set esJavaOpts="-Xmx512m -Xms512m" `
#  --set resources.requests.cpu="100m" --set resources.requests.memory="1Gi" `
#  --set resources.limits.cpu="1000m" --set resources.limits.memory="2Gi" `
#  --set volumeClaimTemplate.storageClassName="standard" `
#  --set clusterHealthCheckParams="wait_for_status=yellow&timeout=1s"

# Получаем секреты
#kubectl get secret elasticsearch-master-credentials -n logging -o yaml

# Для расшифровки
# wsl
# echo bTR1VkgyNlJGY21MaWQ2cw== | base64 -d
# {bTR1VkgyNlJGY21MaWQ2cw==} вместо устанавливаем сначала пароль, потом логин из результата команды выше

# Скачиваем чарт Kibana версии 8.5.1
#Invoke-WebRequest -Uri "https://helm.elastic.co/helm/kibana/kibana-8.5.1.tgz" -OutFile ".\kibana-8.5.1.tgz"

#helm install kibana .\kibana-8.5.1.tgz -n logging `
#  --set elasticsearchHosts="https://elasticsearch-master:9200" `
 # --set elasticsearchCredentialSecret=elasticsearch-master-credentials `
#  --set elasticsearchCertificateSecret=elasticsearch-master-certs `
#  --set service.type=NodePort `
#  --set replicas=1

# Пробрасываем порт к кибане
#kubectl port-forward svc/kibana-kibana -n logging 5601:5601


# Скачиваем чарт Filebeat версии 8.5.1
#Invoke-WebRequest -Uri "https://helm.elastic.co/helm/filebeat/filebeat-8.5.1.tgz" -OutFile ".\filebeat-8.5.1.tgz"

#helm upgrade --install filebeat .\filebeat-8.5.1.tgz -n logging `
#  --set elasticsearchHosts="https://elasticsearch-master:9200" `
#  --set elasticsearchCredentialSecret=elasticsearch-master-credentials `
 # --set elasticsearchCertificateSecret=elasticsearch-master-certs `
 # --set replicas=1