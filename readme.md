### режим совместимости `-P:healthcheck.backward-compatibility=true`

### майлстоуны
- [x] репа в ББ
- [x] приложение, которое не умеет ничего
- [x] приложение, у которого есть /health
- [x] приложение, которое умеет опрашивать другие приложения
- [x] приложение, которое умеет парсить простейший конфиг старого хелсчека
- [x] приложение, которое умеет парсить конфиг со списком сервисов
- [x] приложение, которое умеет парсить конфиг со вторым плечом
- [x] новый конфиг
- [x] режим совместимости со старым конфигом
- [x] опрашивать второе плечо
- [x] опрашивать несколько вторых плеч 
- [x] свой трешхолд для каждого сервиса
- [x] свой дилей для каждого сервиса 
  - [ ] тесты
- [x] кидать эксепшн если список сервисов/плечей содержит дубликаты

### пример старого конфига

```yaml
logging:
  level:
    root: INFO
schedule:
  enabled: "true" # включить хелсчек сервисов и гео-хелсчека на другом плече
  delay: 2000 #  время между хелсчеками сервисов в ms
client-services:
  service-list: # список сервисов, которые надо опрашивать
    - service-name: "attachment-svc"
      port: "80"
      path: "/actuator/health"
failure-threshold: "3" # количество отрицательных ответов от сервисов, после которого плечо выводится из балансировки

geo-healthcheck:
  service-name: "${DO.SECOND_GEO_ENDPOINT_HOSTNAME}" # роут второго плеча
  port: "80"
```

### пример нового конфига

```yaml
logging:
  level:
    root: INFO
schedule:
  enabled: "true"
  delay: 2000
client-services:
  service-list:
    - service-name: "s1"
      port: "80"
      path: "/actuator/health"
      failure-threshold: 3
      delay: 3000
    - service-name: "s2"
      port: "80"
      path: "/actuator/health"
      failure-threshold: 2
      delay: 2000


geo-healthcheck-list:
  - service-name: "geo1"
    port: "80"
  - service-name: geo2
    port: 80
```



### примеры ответов
```json
{
  "status": "success",
  "code": "200",
  "namespace": "имя неймспейса, можно взять из переменных окружения"
}
```
```json
{
  "status": "error",
  "code": "500",
  "namespace": "имя неймспейса, можно взять из переменных окружения"
}
```
переменная окружения с неймспейсом `POD_NAMESPACE`