# Tutorial Completo: CI/CD con Jenkins, Docker, Kubernetes y Microservicios

## Sistema de Pedidos de Comida - Guia paso a paso

---

## Tabla de Contenidos

1. [Vision General del Sistema](#1-vision-general-del-sistema)
2. [Arquitectura de Alto Nivel](#2-arquitectura-de-alto-nivel)
3. [Prerequisitos](#3-prerequisitos)
4. [Estructura del Proyecto](#4-estructura-del-proyecto)
5. [Capa 1: Los Microservicios (Spring Boot)](#5-capa-1-los-microservicios-spring-boot)
6. [Capa 2: Docker - Contenerizacion](#6-capa-2-docker---contenerizacion)
7. [Capa 3: Kubernetes - Orquestacion](#7-capa-3-kubernetes---orquestacion)
8. [Capa 4: Helm - Gestion de Despliegues](#8-capa-4-helm---gestion-de-despliegues)
9. [Capa 5: Jenkins - Automatizacion CI/CD](#9-capa-5-jenkins---automatizacion-cicd)
10. [Capa 6: Tests E2E con Bruno](#10-capa-6-tests-e2e-con-bruno)
11. [Flujo Completo del Pipeline](#11-flujo-completo-del-pipeline)
12. [Guia Paso a Paso: Levantar Todo](#12-guia-paso-a-paso-levantar-todo)
13. [Troubleshooting](#13-troubleshooting)
14. [Plugins de Jenkins para Visualizar Stages](#14-plugins-de-jenkins-para-visualizar-stages)
15. [Jenkins Remoto (Servidor en AWS/Cloud)](#15-jenkins-remoto-servidor-en-awscloud)
16. [Como se Conecta Jenkins con Kubernetes](#16-como-se-conecta-jenkins-con-kubernetes)
17. [Glosario](#17-glosario)

---

## 1. Vision General del Sistema

### Que estamos construyendo?

Un sistema de pedidos de comida compuesto por **6 microservicios independientes** que se comunican entre si. Cada servicio tiene su propia base de datos y se despliega de forma autonoma.

### Por que tantas tecnologias?

Cada tecnologia resuelve un problema especifico:

| Tecnologia | Problema que resuelve |
|---|---|
| **Spring Boot** | Crear los microservicios (logica de negocio) |
| **Docker** | Empaquetar cada servicio para que funcione igual en cualquier maquina |
| **Kubernetes (K8s)** | Orquestar los contenedores: escalar, reiniciar, balancear carga |
| **Helm** | Gestionar las configuraciones de K8s (como un "package manager" para K8s) |
| **Jenkins** | Automatizar todo: compilar, testear, construir imagenes, desplegar |
| **Bruno** | Ejecutar tests E2E (end-to-end) contra las APIs |
| **Kafka** | Comunicacion asincrona entre servicios (eventos) |
| **PostgreSQL** | Base de datos relacional (una por servicio) |

---

## 2. Arquitectura de Alto Nivel

### Diagrama General del Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USUARIOS / CLIENTES                          │
│                    (Web browser, Mobile app)                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP requests
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     API GATEWAY (:8080)                              │
│                 (Spring Cloud Gateway)                               │
│                                                                     │
│  Rutas:                                                             │
│    /api/v1/users/**       → users-service:8081                      │
│    /api/v1/orders/**      → orders-service:8082                     │
│    /api/v1/restaurants/** → catalog-service:8083                    │
│    /api/v1/products/**    → catalog-service:8083                    │
│    /api/v1/payments/**    → payments-service:8084                   │
│    /api/v1/deliveries/**  → deliveries-service:8085                │
│    /api/v1/drivers/**     → deliveries-service:8085                │
└──────┬──────────┬──────────┬──────────┬──────────┬─────────────────┘
       │          │          │          │          │
       ▼          ▼          ▼          ▼          ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│  Users   ││  Orders  ││ Catalog  ││ Payments ││Deliveries│
│ Service  ││ Service  ││ Service  ││ Service  ││ Service  │
│  :8081   ││  :8082   ││  :8083   ││  :8084   ││  :8085   │
└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘
     │           │           │           │           │
     │           └─────┬─────┘           │           │
     │                 │ Kafka Events    │           │
     │           ┌─────┴─────┐           │           │
     │           │   KAFKA   │           │           │
     │           │   :9092   │           │           │
     │           └───────────┘           │           │
     ▼           ▼           ▼           ▼           ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│users_db  ││orders_db ││catalog_db││payments  ││deliveries│
│ :5432    ││ :5433    ││ :5434    ││  _db     ││  _db     │
│          ││          ││          ││ :5435    ││ :5436    │
└──────────┘└──────────┘└──────────┘└──────────┘└──────────┘
  PostgreSQL   PostgreSQL   PostgreSQL  PostgreSQL  PostgreSQL
```

### Diagrama del Pipeline CI/CD

```
┌─────────────────────────────────────────────────────────────────────┐
│                    JENKINS PIPELINE (7 Stages)                      │
│                                                                     │
│  ┌──────────┐  ┌──────────────┐  ┌────────────────┐                │
│  │    1.    │  │     2.       │  │      3.        │                │
│  │ Checkout │─▶│ Build &      │─▶│ Build Docker   │                │
│  │  (git)   │  │ Unit Tests   │  │ Images (x6)    │                │
│  │          │  │ (mvn verify) │  │ (secuencial)   │                │
│  └──────────┘  └──────────────┘  └───────┬────────┘                │
│                                          │                         │
│  ┌──────────────┐  ┌──────────────┐  ┌───▼────────────┐           │
│  │     6.       │  │     5.       │  │      4.        │           │
│  │ Wait for     │◀─│ Deploy with  │◀─│ Load Images    │           │
│  │ Services     │  │ Helm         │  │ to K8s         │           │
│  │ (kubectl     │  │ (helm        │  │ (si minikube)  │           │
│  │  wait)       │  │  upgrade)    │  │                │           │
│  └──────┬───────┘  └──────────────┘  └────────────────┘           │
│         │                                                          │
│  ┌──────▼───────┐                                                  │
│  │     7.       │  Si FALLA: muestra logs de pods                  │
│  │ E2E Tests    │  Si OK: "Pipeline SUCCESS"                       │
│  │ (Bruno CLI)  │                                                  │
│  │ 6 suites     │                                                  │
│  └──────────────┘                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

### Diagrama de Red: Como se conecta Jenkins con K8s

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOCKER DESKTOP (Windows)                      │
│                                                                 │
│  ┌──────────────────┐      ┌──────────────────────────────┐    │
│  │ Jenkins Container │      │ Kubernetes Cluster            │    │
│  │ (jenkins-local)   │      │ (Docker Desktop K8s)          │    │
│  │                   │      │                               │    │
│  │  Puerto: 9090     │      │  Namespace: food-ordering-e2e │    │
│  │                   │      │                               │    │
│  │  Tiene:           │      │  ┌─────────┐ ┌─────────┐     │    │
│  │  - Maven 3.9      │      │  │users-svc│ │orders-svc│    │    │
│  │  - Docker CLI ────────────│──│         │ │         │     │    │
│  │  - kubectl ───────────────│──│catalog  │ │payments │     │    │
│  │  - Helm ──────────────────│──│         │ │         │     │    │
│  │  - Bruno CLI      │      │  │delivery │ │gateway  │     │    │
│  │                   │      │  └─────────┘ └────┬────┘     │    │
│  │                   │      │               NodePort       │    │
│  │  E2E Tests ───────────── host.docker.internal:XXXXX     │    │
│  │                   │      │                               │    │
│  └──────────────────┘      └──────────────────────────────┘    │
│          │                          │                           │
│          │    /var/run/docker.sock   │                           │
│          └──────────┬───────────────┘                           │
│                     │ (Docker Socket compartido)                │
└─────────────────────┘───────────────────────────────────────────┘
```

**Explicacion del diagrama:**
- Jenkins corre DENTRO de un contenedor Docker
- Jenkins accede a Docker del host via el socket `/var/run/docker.sock`
- Jenkins accede a K8s via `kubectl` (config montada desde `~/.kube`)
- Los tests E2E desde Jenkins llegan al API Gateway via `host.docker.internal:NodePort`

---

## 3. Prerequisitos

### Software necesario

| Software | Version | Para que se usa |
|---|---|---|
| **Docker Desktop** | 4.x+ | Contenedores + K8s local |
| **Git** | 2.x+ | Control de versiones |
| **Java JDK** | 25 | Compilar los microservicios (local) |
| **Maven** | 3.9.x | Build tool (local) |

### Activar Kubernetes en Docker Desktop

1. Abrir Docker Desktop
2. Ir a **Settings** > **Kubernetes**
3. Marcar **Enable Kubernetes**
4. Click **Apply & Restart**
5. Esperar a que el indicador de K8s este en verde

### Verificar instalacion

```bash
# Docker funcionando
docker --version
docker ps

# Kubernetes funcionando
kubectl cluster-info
kubectl get nodes

# Git configurado
git --version
```

---

## 4. Estructura del Proyecto

```
SistemaPedidos/
│
├── pom.xml                          # POM padre (Maven multi-module)
├── Jenkinsfile                      # Definicion del pipeline CI/CD
├── docker-compose.yml               # Entorno local completo
├── .dockerignore                    # Archivos excluidos del Docker build
│
├── common/                          # Modulo compartido (eventos, DTOs)
│   ├── pom.xml
│   └── src/
│
├── users-service/                   # Microservicio de Usuarios
│   ├── pom.xml
│   ├── Dockerfile                   # Imagen Docker del servicio
│   └── src/
│       └── main/
│           ├── java/                # Codigo Java (hexagonal architecture)
│           └── resources/
│               ├── application.yml  # Configuracion Spring Boot
│               └── db/migration/    # Flyway migrations (SQL)
│
├── orders-service/                  # Microservicio de Pedidos
├── catalog-service/                 # Microservicio de Catalogo
├── payments-service/                # Microservicio de Pagos
├── deliveries-service/              # Microservicio de Entregas
├── api-gateway/                     # API Gateway (Spring Cloud Gateway)
│
├── infrastructure/
│   ├── jenkins/
│   │   ├── Dockerfile               # Imagen personalizada de Jenkins
│   │   ├── docker-compose.yml       # Para levantar Jenkins
│   │   └── plugins.txt              # Plugins de Jenkins pre-instalados
│   ├── prometheus/
│   │   └── prometheus.yml           # Config de metricas
│   └── logstash/
│       └── logstash.conf            # Config de logs centralizados
│
├── helm/
│   └── food-ordering-system/        # Helm Chart (umbrella)
│       ├── Chart.yaml               # Definicion del chart padre
│       ├── values.yaml              # Valores por defecto
│       ├── templates/               # Templates de infraestructura
│       │   ├── namespace.yaml       # Crea el namespace
│       │   ├── secrets.yaml         # Secrets de base de datos
│       │   ├── databases.yaml       # 5 PostgreSQL StatefulSets
│       │   ├── kafka.yaml           # Kafka + Zookeeper
│       │   └── ingress.yaml         # Ingress rules (rutas)
│       ├── environments/            # Valores por ambiente
│       │   ├── values-dev.yaml
│       │   ├── values-staging.yaml
│       │   ├── values-e2e.yaml      # Usado por Jenkins
│       │   └── values-prod.yaml
│       └── charts/                  # Subcharts (uno por servicio)
│           ├── users-service/
│           │   ├── Chart.yaml
│           │   ├── values.yaml
│           │   └── templates/
│           │       ├── deployment.yaml  # Pod definition
│           │       ├── service.yaml     # Network service
│           │       └── hpa.yaml         # Autoscaling
│           ├── orders-service/
│           ├── catalog-service/
│           ├── payments-service/
│           ├── deliveries-service/
│           └── api-gateway/
│
├── bruno-collection/                # Tests E2E
│   ├── environments/
│   │   ├── local.bru                # URLs para desarrollo local
│   │   ├── docker.bru               # URLs para docker-compose
│   │   └── k8s.bru                  # URLs para Kubernetes
│   ├── 1-users/                     # Tests de usuarios
│   ├── 2-catalog/                   # Tests de catalogo
│   ├── 3-orders/                    # Tests de pedidos
│   ├── 4-payments/                  # Tests de pagos
│   ├── 5-deliveries/                # Tests de entregas
│   └── 6-drivers/                   # Tests de conductores
│
├── scripts/
│   └── health-check.sh              # Verifica salud de pods en K8s
│
└── docs/
    └── CI-CD-TUTORIAL.md            # Este documento
```

---

## 5. Capa 1: Los Microservicios (Spring Boot)

### Que es un microservicio?

Es una aplicacion pequena e independiente que hace UNA cosa bien. En nuestro caso:

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `users-service` | 8081 | Registro y gestion de usuarios |
| `orders-service` | 8082 | Creacion y seguimiento de pedidos |
| `catalog-service` | 8083 | Restaurantes y productos |
| `payments-service` | 8084 | Procesar pagos y reembolsos |
| `deliveries-service` | 8085 | Entregas y conductores |
| `api-gateway` | 8080 | Punto de entrada unico (enruta a los demas) |

### Por que un API Gateway?

Sin gateway, los clientes tendrian que conocer 5 URLs diferentes. El gateway unifica todo:

```
SIN Gateway:                          CON Gateway:
─────────────                         ────────────
Cliente → localhost:8081/api/users    Cliente → localhost:8080/api/v1/users
Cliente → localhost:8082/api/orders   Cliente → localhost:8080/api/v1/orders
Cliente → localhost:8083/api/restaurants  Cliente → localhost:8080/api/v1/restaurants
```

### Configuracion del Gateway (application.yml)

```yaml
# api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: users-service
          uri: ${USERS_SERVICE_URL:http://localhost:8081}
          predicates:
            - Path=/api/v1/users/**
        - id: orders-service
          uri: ${ORDERS_SERVICE_URL:http://localhost:8082}
          predicates:
            - Path=/api/v1/orders/**
        # ... mas rutas
```

Las URLs (`USERS_SERVICE_URL`, etc.) se inyectan como variables de entorno. Esto permite que el mismo codigo funcione en desarrollo local y en Kubernetes.

### Patron Database-per-Service

Cada servicio tiene su propia base de datos PostgreSQL. Esto garantiza:
- **Independencia**: un servicio puede cambiar su schema sin afectar a otros
- **Aislamiento**: si una BD falla, solo afecta a un servicio

```
users-service    → users_db    (puerto 5432)
orders-service   → orders_db   (puerto 5433)
catalog-service  → catalog_db  (puerto 5434)
payments-service → payments_db (puerto 5435)
deliveries-service → deliveries_db (puerto 5436)
```

### Comunicacion entre servicios via Kafka

Los servicios se comunican de forma **asincrona** usando eventos Kafka:

```
orders-service                      payments-service
     │                                    │
     │  Publica: OrderCreatedEvent        │
     ├──────────────► KAFKA ──────────────►│
     │                                    │  Procesa pago
     │                                    │
     │  Publica: PaymentCompletedEvent    │
     │◄────────────── KAFKA ◄─────────────┤
     │                                    │
     │  Actualiza estado del pedido
```

---

## 6. Capa 2: Docker - Contenerizacion

### Que problema resuelve Docker?

"En mi maquina funciona" → Docker garantiza que funcione IGUAL en todas partes.

### Dockerfile de un microservicio (optimizado)

```dockerfile
# users-service/Dockerfile
FROM eclipse-temurin:25-jre       # Imagen base: solo Java Runtime (no JDK completo)
WORKDIR /app                       # Directorio de trabajo dentro del contenedor

# Seguridad: crear usuario no-root
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copiar el JAR pre-compilado (ya compilado por Maven en Jenkins)
COPY users-service/target/*.jar app.jar

# El usuario appuser es dueno de los archivos
RUN chown -R appuser:appgroup /app
USER appuser                       # Ejecutar como usuario no-root

EXPOSE 8081                        # Puerto que expone el servicio

ENTRYPOINT ["java", "-jar", "app.jar"]  # Comando para ejecutar
```

### Por que esta optimizacion es importante?

**ANTES (lento - 5-10 min por imagen x 6 = 30-60 min):**

```dockerfile
# Version anterior - Maven DENTRO del Docker
FROM maven:3.9-eclipse-temurin-25 AS builder    # Descarga Maven + JDK (800MB)
WORKDIR /app
COPY pom.xml .                                   # Copia todos los POMs
COPY common/ common/                             # Copia todo el source code
COPY users-service/ users-service/
RUN mvn -pl users-service package -DskipTests    # Compila DENTRO del Docker

FROM eclipse-temurin:25-jre
COPY --from=builder /app/users-service/target/*.jar app.jar
```

**AHORA (rapido - 5 segundos por imagen):**

```dockerfile
# Version optimizada - Solo copia el JAR
FROM eclipse-temurin:25-jre        # Solo JRE (200MB)
COPY users-service/target/*.jar app.jar  # JAR ya compilado
```

```
Comparacion de tiempos:
────────────────────────────────────────────
ANTES:  [Maven download 2min] + [Compile 3min] x 6 servicios = ~30 min
AHORA:  [Copy JAR 5seg] x 6 servicios (en paralelo)          = ~10 seg
────────────────────────────────────────────
```

### .dockerignore - Excluir archivos innecesarios

```
# .dockerignore - Estrategia "deny all, allow only needed"
# Ignora TODO por defecto
**

# Solo permite los JARs compilados y Dockerfiles que el build necesita
!*/Dockerfile
!users-service/target/*.jar
!orders-service/target/*.jar
!catalog-service/target/*.jar
!payments-service/target/*.jar
!deliveries-service/target/*.jar
!api-gateway/target/*.jar
```

Sin `.dockerignore`, Docker envia TODO el proyecto como "build context" al daemon (~500MB+). Con la estrategia de "negar todo y permitir solo lo necesario", el contexto se reduce a unos pocos MB (solo los fat JARs).

### docker-compose.yml - Entorno local completo

El `docker-compose.yml` en la raiz levanta TODO el sistema localmente:

```yaml
# Fragmento simplificado
services:
  # 5 bases de datos PostgreSQL
  users-db:
    image: postgres:16-alpine
    ports: ["5432:5432"]
    environment:
      POSTGRES_DB: users_db
      POSTGRES_USER: users_user
      POSTGRES_PASSWORD: users_pass

  # Kafka para eventos
  kafka:
    image: confluentinc/cp-kafka:7.6.0
    ports: ["9092:9092"]

  # Los 6 microservicios
  users-service:
    build:
      context: .
      dockerfile: users-service/Dockerfile
    ports: ["8081:8081"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://users-db:5432/users_db
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on: [users-db, kafka]

  # Observabilidad
  prometheus:
    image: prom/prometheus:v2.51.0
    ports: ["9091:9090"]

  grafana:
    image: grafana/grafana:10.4.0
    ports: ["3000:3000"]
```

---

## 7. Capa 3: Kubernetes - Orquestacion

### Que problema resuelve Kubernetes?

Docker ejecuta contenedores. Kubernetes los **gestiona en produccion**:
- **Autorecuperacion**: si un pod muere, K8s lo reinicia
- **Escalado**: aumenta/disminuye replicas segun la carga
- **Balanceo de carga**: distribuye trafico entre replicas
- **Actualizaciones sin downtime**: rolling updates

### Conceptos clave de K8s

```
┌─────────────────────────────────────────────────┐
│ CLUSTER (Docker Desktop Kubernetes)             │
│                                                 │
│  ┌───────────────────────────────────────────┐  │
│  │ NAMESPACE: food-ordering-e2e              │  │
│  │                                           │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │ DEPLOYMENT: users-service           │  │  │
│  │  │                                     │  │  │
│  │  │  ┌───────────┐  ┌───────────┐      │  │  │
│  │  │  │ POD       │  │ POD       │      │  │  │
│  │  │  │ (replica1)│  │ (replica2)│      │  │  │
│  │  │  │           │  │           │      │  │  │
│  │  │  │ Container │  │ Container │      │  │  │
│  │  │  │ users-svc │  │ users-svc │      │  │  │
│  │  │  └───────────┘  └───────────┘      │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │                                           │  │
│  │  ┌──────────────────┐                     │  │
│  │  │ SERVICE          │                     │  │
│  │  │ (ClusterIP)      │ ← Balancea trafico  │  │
│  │  │ users-service    │   entre los pods     │  │
│  │  │ :8081            │                     │  │
│  │  └──────────────────┘                     │  │
│  │                                           │  │
│  │  ┌──────────────────┐                     │  │
│  │  │ SECRET           │                     │  │
│  │  │ users-db-secret  │ ← Credenciales DB   │  │
│  │  │ url, user, pass  │                     │  │
│  │  └──────────────────┘                     │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### Recursos de K8s en nuestro proyecto

| Recurso K8s | Que hace | Archivo |
|---|---|---|
| **Namespace** | Aislamiento logico del entorno | `templates/namespace.yaml` |
| **Deployment** | Define cuantos pods y como crearlos | `charts/*/templates/deployment.yaml` |
| **Service** | Expone pods dentro del cluster (DNS interno) | `charts/*/templates/service.yaml` |
| **Secret** | Almacena credenciales (base64) | `templates/secrets.yaml` |
| **StatefulSet** | Bases de datos con almacenamiento persistente | `templates/databases.yaml` |
| **HPA** | Horizontal Pod Autoscaler (escala automatico) | `charts/*/templates/hpa.yaml` |
| **Ingress** | Expone servicios al exterior con rutas HTTP | `templates/ingress.yaml` |

### Deployment Template (ejemplo: users-service)

```yaml
# helm/food-ordering-system/charts/users-service/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-service
  namespace: food-ordering-e2e
spec:
  replicas: 1                          # Cuantos pods
  selector:
    matchLabels:
      app.kubernetes.io/name: users-service
  template:
    spec:
      containers:
        - name: users-service
          image: "food-ordering/users-service:15"   # Imagen Docker
          imagePullPolicy: Never                     # Usar imagen local
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_DATASOURCE_URL            # Desde Secret
              valueFrom:
                secretKeyRef:
                  name: users-db-secret
                  key: url
            - name: SPRING_PROFILES_ACTIVE           # Perfil sin seguridad
              value: nosecurity
          readinessProbe:                            # K8s verifica salud
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 30                  # Espera 30s antes de verificar
            periodSeconds: 10                        # Verifica cada 10s
          livenessProbe:                             # Si falla, reinicia el pod
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 60
            periodSeconds: 30
```

### Probes: Como K8s sabe si un pod esta sano

```
Timeline de un Pod:
─────────────────────────────────────────────────────────────
t=0s     Pod arranca (java -jar app.jar)
t=0-30s  Spring Boot inicializando, Flyway migrations...
t=30s    readinessProbe empieza: GET /actuator/health
         Si responde 200 → Pod marcado como "Ready"
         Si falla → Pod NO recibe trafico (pero sigue vivo)
t=60s    livenessProbe empieza: GET /actuator/health
         Si responde 200 → Pod sigue vivo
         Si falla 3 veces → K8s REINICIA el pod
─────────────────────────────────────────────────────────────
```

---

## 8. Capa 4: Helm - Gestion de Despliegues

### Que problema resuelve Helm?

Sin Helm, tendrias que aplicar decenas de archivos YAML manualmente:

```bash
# Sin Helm (tedioso y propenso a errores):
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml
kubectl apply -f databases.yaml
kubectl apply -f kafka.yaml
kubectl apply -f users-deployment.yaml
kubectl apply -f users-service.yaml
kubectl apply -f orders-deployment.yaml
# ... y asi con cada archivo
```

Con Helm, UN solo comando:

```bash
# Con Helm:
helm upgrade --install food-ordering helm/food-ordering-system \
  -f helm/food-ordering-system/environments/values-e2e.yaml \
  --namespace food-ordering-e2e
```

### Estructura del Helm Chart

```
helm/food-ordering-system/          ← Chart "paraguas" (umbrella)
├── Chart.yaml                       ← Define dependencias (subcharts)
├── values.yaml                      ← Valores por defecto
├── environments/                    ← Valores por ambiente
│   ├── values-dev.yaml
│   ├── values-staging.yaml
│   ├── values-e2e.yaml             ← Jenkins usa este
│   └── values-prod.yaml
├── templates/                       ← Infraestructura compartida
│   ├── namespace.yaml
│   ├── secrets.yaml                 ← Credenciales de BD
│   ├── databases.yaml               ← 5 PostgreSQL
│   ├── kafka.yaml                   ← Kafka + Zookeeper
│   └── ingress.yaml                 ← Rutas HTTP
└── charts/                          ← Un subchart por servicio
    ├── users-service/
    ├── orders-service/
    ├── catalog-service/
    ├── payments-service/
    ├── deliveries-service/
    └── api-gateway/
```

### Ambientes: Un mismo chart, diferentes configuraciones

```
┌──────────────────────────────────────────────────────────────────┐
│                    Mismo Helm Chart                               │
│              helm/food-ordering-system/                           │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  values-     │  │  values-     │  │  values-prod.yaml    │   │
│  │  e2e.yaml    │  │  staging.yaml│  │                      │   │
│  │              │  │              │  │  replicas: 3          │   │
│  │  replicas: 1 │  │  replicas: 2 │  │  HPA: 3-10 pods     │   │
│  │  HPA: off    │  │  HPA: 1-3   │  │  memory: 512Mi-1Gi   │   │
│  │  memory:128Mi│  │  memory:256Mi│  │  TLS: enabled        │   │
│  │  nosecurity  │  │  JWT auth    │  │  JWT auth            │   │
│  │  infra: yes  │  │  infra: ext  │  │  infra: external     │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
│        │                  │                    │                  │
│        ▼                  ▼                    ▼                  │
│  ┌──────────┐      ┌──────────┐       ┌──────────────┐          │
│  │ Namespace│      │ Namespace│       │ Namespace    │          │
│  │ food-    │      │ food-    │       │ food-ordering│          │
│  │ordering- │      │ordering- │       │ -prod        │          │
│  │ e2e      │      │ staging  │       │              │          │
│  └──────────┘      └──────────┘       └──────────────┘          │
└──────────────────────────────────────────────────────────────────┘
```

### global.imageTag: Como el pipeline fuerza nuevas imagenes

Problema: si siempre usas tag `latest`, K8s no sabe que la imagen cambio y no recrea los pods.

Solucion: cada build de Jenkins usa un tag unico (el numero de build):

```yaml
# En el deployment template:
image: "food-ordering/users-service:{{ .Values.global.imageTag | default .Values.image.tag }}"

# Jenkins ejecuta:
helm upgrade --install ... --set global.imageTag=15

# Resultado: K8s ve que la imagen cambio de :14 a :15 → recrea pods
```

---

## 9. Capa 5: Jenkins - Automatizacion CI/CD

### Que es CI/CD?

- **CI (Continuous Integration)**: Cada push compila, testea y construye imagenes automaticamente
- **CD (Continuous Deployment)**: Despliega automaticamente a un entorno (e2e, staging, prod)

### La imagen Docker personalizada de Jenkins

Jenkins necesita herramientas especiales para nuestro pipeline. Por eso creamos una imagen custom:

```dockerfile
# infrastructure/jenkins/Dockerfile
FROM jenkins/jenkins:lts-jdk21          # Jenkins base

USER root                               # Necesitamos instalar como root

# JDK 25 (nuestros microservicios usan Java 25)
RUN wget "https://...OpenJDK25U-jdk..." -O /tmp/jdk25.tar.gz && \
    tar xzf /tmp/jdk25.tar.gz -C /opt/java/

# Maven 3.9.9 (para compilar)
RUN curl -fsSL https://...maven-3.9.9-bin.tar.gz | tar xz -C /opt

# Docker CLI (para construir imagenes desde Jenkins)
RUN apt-get install -y docker-ce-cli docker-compose-plugin

# kubectl (para interactuar con Kubernetes)
RUN curl -LO "https://dl.k8s.io/release/.../kubectl" && chmod +x kubectl

# Helm (para desplegar charts)
RUN curl -fsSL https://...get-helm-3 | bash

# Node.js + Bruno CLI (para tests E2E)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    npm install -g @usebruno/cli

USER jenkins                            # Volver al usuario Jenkins
```

### docker-compose de Jenkins

```yaml
# infrastructure/jenkins/docker-compose.yml
services:
  jenkins:
    build: .                            # Construye nuestra imagen custom
    container_name: jenkins-local
    ports:
      - "9090:8080"                     # Jenkins UI en localhost:9090
      - "50000:50000"                   # Puerto para agentes
    group_add:
      - "${DOCKER_GID:-0}"             # Permiso al Docker socket
    volumes:
      - jenkins-data:/var/jenkins_home  # Datos persistentes
      - /var/run/docker.sock:/var/run/docker.sock  # Docker-in-Docker
      - ${HOME}/.kube:/var/jenkins_home/.kube:ro    # Config K8s (read-only)
    environment:
      - JENKINS_ADMIN_ID=admin
      - JENKINS_ADMIN_PASSWORD=admin
```

### Diagrama: Volumenes de Jenkins

```
┌─────────────────────────────────────────────────────────────┐
│ HOST (tu PC Windows con Docker Desktop)                     │
│                                                             │
│  /var/run/docker.sock ──────────┐                          │
│  (Docker daemon socket)         │ mount                    │
│                                 │                          │
│  ~/.kube/config ────────────────┤                          │
│  (credenciales K8s)             │ mount (read-only)        │
│                                 │                          │
│  docker volume: jenkins-data ───┤                          │
│  (jobs, plugins, configs)       │ mount                    │
│                                 ▼                          │
│  ┌──────────────────────────────────────────────────┐      │
│  │ CONTENEDOR JENKINS                                │      │
│  │                                                   │      │
│  │  /var/run/docker.sock  → Ejecuta docker build    │      │
│  │  /var/jenkins_home/.kube/config → kubectl, helm  │      │
│  │  /var/jenkins_home/    → Jobs, workspace, datos  │      │
│  └──────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### El Jenkinsfile explicado stage por stage

```groovy
pipeline {
    agent any    // Ejecutar en cualquier nodo disponible

    options {
        timeout(time: 30, unit: 'MINUTES')  // Maximo 30 minutos
        disableConcurrentBuilds()            // No ejecutar 2 builds a la vez
    }

    environment {
        KUBE_NAMESPACE = 'food-ordering-e2e'   // Namespace de K8s
        IMAGE_REGISTRY = 'food-ordering'        // Prefijo de imagenes
        HELM_RELEASE = 'food-ordering'          // Nombre del release Helm
        HELM_CHART = 'helm/food-ordering-system' // Ruta al chart
    }
```

#### Stage 1: Checkout

```groovy
    stage('Checkout') {
        steps {
            checkout scm   // Clona el repo configurado en el job de Jenkins
        }
    }
```

Lo que hace: `git clone https://github.com/.../sistemapedidos.git`

#### Stage 2: Build & Unit Tests

```groovy
    stage('Build & Unit Tests') {
        steps {
            sh 'mvn clean verify -B'  // Compila TODO y ejecuta tests
        }
        post {
            always {
                // Recolecta resultados de tests para mostrar en Jenkins UI
                junit testResults: '**/target/surefire-reports/*.xml'
            }
        }
    }
```

Lo que hace:
1. `mvn clean` - Limpia compilaciones anteriores
2. `mvn verify` - Compila los 7 modulos + ejecuta tests unitarios
3. Genera JARs en `*/target/*.jar` (listos para Docker)
4. Jenkins muestra resultados de tests en su UI

#### Stage 3: Build Docker Images (secuencial)

```groovy
    stage('Build Docker Images') {
        steps {
            script {
                def services = ['users-service', 'orders-service', ...]
                // Build secuencial para evitar saturar Docker Desktop con IO
                services.each { service ->
                    sh "docker build -f ${service}/Dockerfile \
                        -t food-ordering/${service}:${BUILD_NUMBER} \
                        -t food-ordering/${service}:latest ."
                }
            }
        }
    }
```

Lo que hace:
- Construye 6 imagenes Docker **en paralelo** (no secuencial)
- Cada imagen se tagea con el numero de build (ej: `:15`) Y con `:latest`
- Como los Dockerfiles solo copian JARs, cada build toma ~5 segundos

```
Antes (secuencial con Maven):          Ahora (paralelo sin Maven):
─────────────────────────               ────────────────────────
users:    [████████ 5min]               users:    [█ 5s]
orders:   [████████ 5min]               orders:   [█ 5s]  ← en paralelo
catalog:  [████████ 5min]               catalog:  [█ 5s]  ← en paralelo
payments: [████████ 5min]               payments: [█ 5s]  ← en paralelo
delivery: [████████ 5min]               delivery: [█ 5s]  ← en paralelo
gateway:  [████████ 5min]               gateway:  [█ 5s]  ← en paralelo
Total:    ~30 minutos                   Total:    ~10 segundos
```

#### Stage 4: Load Images to K8s

```groovy
    stage('Load Images to K8s') {
        steps {
            script {
                def isMinikube = sh(script: 'minikube status', returnStatus: true) == 0
                if (isMinikube) {
                    // Minikube tiene su propio Docker daemon
                    services.each { sh "minikube image load ..." }
                } else {
                    // Docker Desktop K8s comparte el Docker daemon
                    echo 'Docker Desktop K8s detected - images already available'
                }
            }
        }
    }
```

Lo que hace:
- **Docker Desktop K8s**: No hace nada! Las imagenes ya estan disponibles porque K8s usa el mismo Docker daemon
- **Minikube**: Necesita cargar las imagenes explicitamente

#### Stage 5: Deploy with Helm

```groovy
    stage('Deploy with Helm') {
        steps {
            sh """
                helm dependency update helm/food-ordering-system || true
                helm upgrade --install food-ordering helm/food-ordering-system \
                    -f helm/food-ordering-system/environments/values-e2e.yaml \
                    --set global.imageTag=${BUILD_NUMBER} \
                    --namespace food-ordering-e2e \
                    --create-namespace \
                    --wait \
                    --timeout 8m0s
            """
        }
    }
```

Lo que hace:
1. `helm dependency update` - Descarga/actualiza subcharts
2. `helm upgrade --install` - Instala o actualiza el release:
   - `-f values-e2e.yaml` → Usa configuracion E2E (nosecurity, infra incluida)
   - `--set global.imageTag=15` → Fuerza uso de imagenes con tag del build
   - `--namespace food-ordering-e2e` → Despliega en su propio namespace
   - `--create-namespace` → Crea el namespace si no existe
   - `--wait` → Espera a que todos los pods esten Ready
   - `--timeout 8m0s` → Maximo 8 minutos para que todo arranque

#### Stage 6: Wait for Services + Health Check

```groovy
    stage('Wait for Services') {
        steps {
            sh """
                # Espera infraestructura (DBs, Kafka)
                kubectl wait --for=condition=ready pod -l app=users-db \
                    -n food-ordering-e2e --timeout=120s || true

                # Espera microservicios
                kubectl wait --for=condition=ready pod \
                    -l app.kubernetes.io/name=users-service \
                    -n food-ordering-e2e --timeout=180s
                # ... repite para cada servicio
            """
            sh './scripts/health-check.sh'  // Verificacion extra de salud
        }
    }
```

Lo que hace:
1. `kubectl wait` espera a que cada pod pase de "Starting" a "Ready"
2. `health-check.sh` verifica que la condicion Ready es verdadera para todos

#### Stage 7: E2E Tests con Bruno

```groovy
    stage('E2E Tests - Bruno') {
        steps {
            script {
                // Descubre el NodePort del API Gateway
                def nodePort = sh(
                    script: "kubectl get svc -n food-ordering-e2e \
                        -l app.kubernetes.io/name=api-gateway \
                        -o jsonpath='{.items[0].spec.ports[0].nodePort}'",
                    returnStdout: true
                ).trim()

                // URL accesible desde Jenkins container
                def gatewayUrl = "http://host.docker.internal:${nodePort}"

                // Crea environment dinamico para Bruno
                writeFile file: 'bruno-collection/environments/jenkins.bru',
                    text: "vars { base_url: ${gatewayUrl} ... }"

                // Ejecuta tests secuenciales
                sh """
                    cd bruno-collection
                    bru run --env jenkins 1-users/
                    bru run --env jenkins 2-catalog/
                    bru run --env jenkins 3-orders/
                    bru run --env jenkins 4-payments/
                    bru run --env jenkins 5-deliveries/
                    bru run --env jenkins 6-drivers/
                """
            }
        }
    }
```

Lo que hace:
1. Obtiene el **NodePort** asignado al API Gateway (puerto aleatorio que K8s expone)
2. Crea un Bruno environment dinamico con la URL `http://host.docker.internal:XXXXX`
   - `host.docker.internal` es como un contenedor Docker llega al host
3. Ejecuta las 6 suites de tests en orden (usuarios primero porque otros dependen de ellos)

```
Flujo de red durante E2E:
─────────────────────────────────────────────────────
Jenkins Container
    │
    │  bru run → POST http://host.docker.internal:32456/api/v1/users
    │
    ▼
Host (Docker Desktop)
    │
    │  NodePort 32456 → K8s Service api-gateway:8080
    │
    ▼
API Gateway Pod
    │
    │  Ruta /api/v1/users → http://food-ordering-users-service:8081
    │
    ▼
Users Service Pod
    │
    │  Consulta PostgreSQL (users-db:5432)
    │
    ▼
Respuesta viaja de vuelta al test de Bruno
```

---

## 10. Capa 6: Tests E2E con Bruno

### Que es Bruno?

Bruno es una herramienta de testing de APIs (alternativa a Postman) que funciona con archivos de texto:

```bru
# bruno-collection/1-users/1-register-user.bru
meta {
  name: 1. Register User
  type: http
  seq: 1                    # Orden de ejecucion
}

post {
  url: {{users_url}}/api/v1/users     # Usa variable del environment
  body: json
}

body:json {
  {
    "email": "juan.perez@email.com",
    "password": "SecurePass123!",
    "firstName": "Juan",
    "lastName": "Perez",
    "phone": "+56912345678",
    "address": "Av. Providencia 1234, Santiago",
    "role": "CUSTOMER"
  }
}

script:post-response {
  if (res.status === 201) {
    bru.setVar("user_id", res.body.id);  // Guarda ID para tests siguientes
  }
}
```

### Encadenamiento de tests

Los tests estan ordenados y cada uno pasa datos al siguiente:

```
1-users/1-register-user.bru     → crea usuario, guarda user_id
2-catalog/1-create-restaurant.bru → crea restaurante, guarda restaurant_id
2-catalog/5-create-product.bru   → crea producto, guarda product_id
3-orders/1-create-order.bru      → usa user_id + product_id, guarda order_id
4-payments/1-process-payment.bru → usa order_id, guarda payment_id
5-deliveries/1-create-delivery.bru → usa order_id, guarda delivery_id
6-drivers/1-register-driver.bru  → crea driver, guarda driver_id
```

### Environments de Bruno

```
┌─────────────────────────────────────────────────────────────┐
│                  Bruno Environments                          │
│                                                             │
│  local.bru          → http://localhost:8081 (directo)       │
│  docker.bru         → http://localhost:8080 (via gateway)   │
│  k8s.bru            → http://api.food-ordering.local        │
│  jenkins.bru (auto) → http://host.docker.internal:XXXXX    │
└─────────────────────────────────────────────────────────────┘
```

---

## 11. Flujo Completo del Pipeline

### Diagrama secuencial completo

```
DESARROLLADOR          GIT/GITHUB           JENKINS                 KUBERNETES
     │                     │                   │                       │
     │  git push           │                   │                       │
     ├────────────────────►│                   │                       │
     │                     │  webhook/poll     │                       │
     │                     ├──────────────────►│                       │
     │                     │                   │                       │
     │                     │     Stage 1: Checkout                     │
     │                     │◄──────────────────┤ git clone             │
     │                     │──────────────────►│                       │
     │                     │                   │                       │
     │                     │     Stage 2: Build & Test                 │
     │                     │                   │ mvn clean verify      │
     │                     │                   │ 117 tests pass        │
     │                     │                   │ JARs generados        │
     │                     │                   │                       │
     │                     │     Stage 3: Docker Build                 │
     │                     │                   │ 6 imagenes en paralelo│
     │                     │                   │ ~10 segundos          │
     │                     │                   │                       │
     │                     │     Stage 4: Load to K8s                  │
     │                     │                   │ (Docker Desktop: skip)│
     │                     │                   │                       │
     │                     │     Stage 5: Deploy                       │
     │                     │                   │ helm upgrade          │
     │                     │                   ├──────────────────────►│
     │                     │                   │                       │ Crea/actualiza:
     │                     │                   │                       │ - 5 DBs
     │                     │                   │                       │ - Kafka
     │                     │                   │                       │ - 6 services
     │                     │                   │                       │ - Secrets
     │                     │     Stage 6: Wait                         │
     │                     │                   │ kubectl wait          │
     │                     │                   ├──────────────────────►│
     │                     │                   │◄──────────────────────┤ Pods Ready
     │                     │                   │                       │
     │                     │     Stage 7: E2E                          │
     │                     │                   │ bru run 1-users/      │
     │                     │                   │────────── NodePort ──►│──► users-svc
     │                     │                   │ bru run 2-catalog/    │
     │                     │                   │────────── NodePort ──►│──► catalog-svc
     │                     │                   │ bru run 3-orders/     │
     │                     │                   │────────── NodePort ──►│──► orders-svc
     │                     │                   │ ...                   │
     │                     │                   │                       │
     │                     │                   │ Pipeline SUCCESS!     │
     │  Email/Notificacion │                   │                       │
     │◄────────────────────┤◄──────────────────┤                       │
```

---

## 12. Guia Paso a Paso: Levantar Todo

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/gfernandez-engineer/sistemapedidos.git
cd sistemapedidos
```

### Paso 2: Verificar Docker Desktop y Kubernetes

```bash
# Docker funcionando?
docker ps

# K8s funcionando?
kubectl get nodes
# Debe mostrar: docker-desktop   Ready   ...
```

### Paso 3: Levantar Jenkins

```bash
cd infrastructure/jenkins

# Construir y levantar Jenkins
docker-compose up -d --build

# Verificar que Jenkins esta corriendo
docker ps | grep jenkins
# Debe mostrar: jenkins-local ... Up ... 0.0.0.0:9090->8080/tcp
```

### Paso 4: Acceder a Jenkins

1. Abrir navegador: **http://localhost:9090**
2. Credenciales: **admin / admin**

### Paso 5: Crear el Pipeline Job

1. Click **"New Item"** (o "Nueva Tarea")
2. Nombre: `food-ordering-pipeline`
3. Tipo: **Pipeline**
4. Click **OK**

5. En la configuracion del job:
   - Seccion **Pipeline**:
     - Definition: **Pipeline script from SCM**
     - SCM: **Git**
     - Repository URL: `https://github.com/gfernandez-engineer/sistemapedidos.git`
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`
   - Click **Save**

### Paso 6: Ejecutar el Pipeline

1. Click **"Build Now"** (o "Construir ahora")
2. Observar el progreso en **Stage View** o **Console Output**

### Paso 7: Verificar el resultado

```bash
# Ver pods desplegados
kubectl get pods -n food-ordering-e2e

# Debe mostrar algo como:
# NAME                                 READY   STATUS    RESTARTS   AGE
# users-db-0                           1/1     Running   0          5m
# orders-db-0                          1/1     Running   0          5m
# catalog-db-0                         1/1     Running   0          5m
# payments-db-0                        1/1     Running   0          5m
# deliveries-db-0                      1/1     Running   0          5m
# kafka-xxxxx                          1/1     Running   0          5m
# zookeeper-xxxxx                      1/1     Running   0          5m
# food-ordering-users-service-xxxxx    1/1     Running   0          3m
# food-ordering-orders-service-xxxxx   1/1     Running   0          3m
# food-ordering-catalog-service-xxxxx  1/1     Running   0          3m
# food-ordering-payments-service-xxxxx 1/1     Running   0          3m
# food-ordering-deliveries-service-xxx 1/1     Running   0          3m
# food-ordering-api-gateway-xxxxx      1/1     Running   0          3m
```

### Comandos utiles de verificacion

```bash
# Ver todos los recursos del namespace
kubectl get all -n food-ordering-e2e

# Ver logs de un servicio
kubectl logs -n food-ordering-e2e -l app.kubernetes.io/name=users-service

# Ver detalles de un pod que falla
kubectl describe pod <nombre-del-pod> -n food-ordering-e2e

# Ver eventos recientes (util para debugging)
kubectl get events -n food-ordering-e2e --sort-by='.lastTimestamp'

# Ver los services y sus puertos
kubectl get svc -n food-ordering-e2e

# Acceder al API Gateway via NodePort
kubectl get svc -n food-ordering-e2e -l app.kubernetes.io/name=api-gateway
# Buscar el NodePort (ej: 32456) y acceder: http://localhost:32456/actuator/health
```

---

## 13. Troubleshooting

### Error: "permission denied" al Docker socket (Build #12)

**Causa**: Jenkins no tiene permiso para usar Docker del host.

**Solucion**: El `docker-compose.yml` de Jenkins usa `group_add: "${DOCKER_GID:-0}"`. En Docker Desktop Windows, el GID por defecto 0 (root) funciona.

Si persiste:
```bash
# Verificar desde dentro de Jenkins
docker exec jenkins-local docker ps
# Si falla, reconstruir la imagen:
cd infrastructure/jenkins
docker-compose down
docker-compose up -d --build
```

### Error: "not in a git directory" (Build #13)

**Causa**: El workspace de Jenkins se limpio (`cleanWs`) y el checkout fallo.

**Solucion**: Esto se resuelve solo al re-ejecutar el build. `cleanWs()` en el post siempre limpia el workspace al final.

### Error: Docker build tarda demasiado (Build #14)

**Causa**: Los Dockerfiles antiguos ejecutaban Maven DENTRO del Docker.

**Solucion**: Los nuevos Dockerfiles solo copian el JAR. Ya esta corregido.

### Error: "ImagePullBackOff" en pods de K8s

**Causa**: K8s intenta descargar la imagen de un registry remoto.

**Solucion**: Verificar que `imagePullPolicy: Never` esta en values-e2e.yaml:
```yaml
global:
  imagePullPolicy: Never  # Usar imagenes locales
```

### Error: Pods en "CrashLoopBackOff"

**Causa**: El servicio no puede arrancar (generalmente por conexion a DB o Kafka).

```bash
# Ver logs del pod que falla
kubectl logs -n food-ordering-e2e <nombre-del-pod> --tail=50

# Comun: base de datos no lista aun
# Solucion: esperar y dejar que K8s reintente (readinessProbe se encarga)
```

### Error: E2E tests fallan con "connection refused"

**Causa**: El API Gateway no es alcanzable desde Jenkins.

```bash
# Verificar NodePort
kubectl get svc -n food-ordering-e2e -l app.kubernetes.io/name=api-gateway

# Probar conectividad desde Jenkins
docker exec jenkins-local curl -s http://host.docker.internal:<NODE_PORT>/actuator/health
```

### Limpiar todo y empezar de cero

```bash
# Eliminar el namespace de E2E (borra TODOS los pods, services, etc.)
kubectl delete namespace food-ordering-e2e

# Eliminar namespaces viejos (si existen)
kubectl delete namespace order-service product-service user-service 2>/dev/null

# Reconstruir Jenkins (si necesario)
cd infrastructure/jenkins
docker-compose down -v   # -v elimina volumenes (CUIDADO: pierde datos de Jenkins)
docker-compose up -d --build
```

---

## 14. Plugins de Jenkins para Visualizar Stages

### El problema

Por defecto, Jenkins no muestra la tabla visual de stages (barras de colores con tiempos) en la pagina del job. Necesitas plugins adicionales.

### Plugins necesarios

| Plugin | Para que sirve |
|---|---|
| **Pipeline: Stage View** | Tabla visual de stages en la pagina del job |
| **Pipeline: REST API** | Dependencia de Stage View (expone datos de stages via API) |
| **Pipeline: Graph Analysis** | Dependencia de REST API (analiza el grafo del pipeline) |

### Instalacion via UI (Jenkins local o remoto)

1. Ir a **Manage Jenkins > Plugins > Available Plugins**
2. Buscar **"Pipeline: Stage View"**
3. Marcarlo e instalar (las dependencias se resuelven automaticamente)
4. Reiniciar Jenkins

### Instalacion via SSH (servidor remoto sin acceso UI)

```bash
# Conectar al servidor
ssh -i "keyJenkinsServerProd.pem" ubuntu@<IP-del-servidor>

# Descargar los 3 plugins en el directorio de plugins de Jenkins
cd /var/lib/jenkins/plugins
sudo wget -q https://updates.jenkins.io/latest/pipeline-graph-analysis.hpi
sudo wget -q https://updates.jenkins.io/latest/pipeline-rest-api.hpi
sudo wget -q https://updates.jenkins.io/latest/pipeline-stage-view.hpi

# Asignar permisos correctos
sudo chown jenkins:jenkins pipeline-graph-analysis.hpi pipeline-rest-api.hpi pipeline-stage-view.hpi

# Reiniciar Jenkins para cargar los plugins
sudo systemctl restart jenkins
```

> **Nota:** Los stages se visualizan a partir del primer build ejecutado despues de instalar los plugins. Los builds anteriores no muestran la tabla.

---

## 15. Jenkins Remoto (Servidor en AWS/Cloud)

### Diferencia con Jenkins local

| | Jenkins Local | Jenkins Remoto |
|---|---|---|
| **Donde corre** | Docker container en tu PC | Servidor Linux (EC2, VM, etc.) |
| **Herramientas** | Incluidas en el Dockerfile custom | Hay que instalarlas manualmente |
| **K8s** | Docker Desktop Kubernetes | Cluster externo o instalado en el servidor |
| **Acceso** | localhost:9090 | IP-publica:8080 |

### Prerequisitos del servidor

El servidor necesita las siguientes herramientas para ejecutar el pipeline completo:

| Herramienta | Version | Para que |
|---|---|---|
| **JDK 25** | OpenJDK/Temurin 25 | Compilar los microservicios |
| **Maven** | 3.9+ | Build del proyecto |
| **Docker** | 24+ | Construir imagenes de los servicios |
| **kubectl** | 1.28+ | Deploy a Kubernetes |
| **Helm** | 3.x | Gestionar charts de K8s |
| **Node.js** | 20+ | Prerequisito de Bruno CLI |
| **Bruno CLI** | latest | Tests E2E |

### Instalacion paso a paso (Ubuntu/Debian)

```bash
# Conectar al servidor via SSH
ssh -i "keyJenkinsServerProd.pem" ubuntu@<IP-del-servidor>

# ─────────────────────────────────────
# 1. JDK 25 (Eclipse Temurin)
# ─────────────────────────────────────
cd /tmp
wget https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B7/OpenJDK25U-jdk_x64_linux_hotspot_25_7.tar.gz
sudo mkdir -p /usr/lib/jvm
sudo tar -xzf OpenJDK25U-jdk_x64_linux_hotspot_25_7.tar.gz -C /usr/lib/jvm
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-25+7/bin/java 100
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-25+7/bin/javac 100
sudo update-alternatives --set java /usr/lib/jvm/jdk-25+7/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/jdk-25+7/bin/javac
echo 'JAVA_HOME=/usr/lib/jvm/jdk-25+7' | sudo tee -a /etc/environment

# Alternativa: si el repo de Ubuntu lo tiene
# sudo apt install -y openjdk-25-jdk

# Verificar
java -version   # Debe mostrar 25
javac -version  # Debe mostrar 25

# ─────────────────────────────────────
# 2. Docker
# ─────────────────────────────────────
sudo apt-get update
sudo apt-get install -y docker.io
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Verificar
docker --version

# ─────────────────────────────────────
# 3. kubectl
# ─────────────────────────────────────
curl -LO "https://dl.k8s.io/release/$(curl -sL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install kubectl /usr/local/bin/
rm kubectl

# Verificar
kubectl version --client

# ─────────────────────────────────────
# 4. Helm 3
# ─────────────────────────────────────
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verificar
helm version

# ─────────────────────────────────────
# 5. Node.js + Bruno CLI
# ─────────────────────────────────────
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
sudo npm install -g @usebruno/cli

# Verificar
node --version
bru --version

# ─────────────────────────────────────
# 6. Reiniciar Jenkins (para tomar todos los cambios)
# ─────────────────────────────────────
sudo systemctl restart jenkins
```

### Configuracion de Kubernetes

Para que el pipeline pueda hacer deploy, el usuario `jenkins` necesita acceso al cluster K8s:

```bash
# Si el cluster esta en el mismo servidor
sudo mkdir -p /var/lib/jenkins/.kube
sudo cp ~/.kube/config /var/lib/jenkins/.kube/config
sudo chown -R jenkins:jenkins /var/lib/jenkins/.kube
```

### Verificacion rapida

Despues de instalar todo, verificar desde el servidor:

```bash
# Cambiar al usuario jenkins para verificar permisos
sudo -u jenkins bash -c '
  echo "=== Java ===" && java -version &&
  echo "=== Maven ===" && mvn -version &&
  echo "=== Docker ===" && docker --version &&
  echo "=== kubectl ===" && kubectl version --client &&
  echo "=== Helm ===" && helm version &&
  echo "=== Node.js ===" && node --version &&
  echo "=== Bruno ===" && bru --version
'
```

Si todos muestran su version sin error, el pipeline deberia funcionar completo.

---

## 16. Como se Conecta Jenkins con Kubernetes

### Arquitectura de la conexion (entorno local)

```
┌──────────────────────────────────────────────────────────┐
│  Tu maquina (Windows / macOS / Linux)                    │
│                                                          │
│  ┌──────────────┐    ┌───────────────────────────────┐   │
│  │  Jenkins      │    │  Docker Desktop                │   │
│  │  (localhost:  │    │  ┌──────────────────────────┐  │   │
│  │   9090)       │    │  │  Kubernetes cluster       │  │   │
│  │               │────│──│  (single-node)            │  │   │
│  │  Ejecuta:     │    │  │                            │  │   │
│  │  - kubectl    │    │  │  Pods: DBs, Kafka,        │  │   │
│  │  - helm       │    │  │  microservices, gateway    │  │   │
│  │  - docker     │    │  └──────────────────────────┘  │   │
│  └──────────────┘    └───────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

### El archivo kubeconfig es la clave

Cuando ejecutas `kubectl` o `helm`, estos comandos leen el archivo `~/.kube/config` para saber:

- **Donde** esta el cluster → `https://kubernetes.docker.internal:6443` (API server)
- **Como** autenticarse → certificados client TLS (autenticacion mutua)
- **Que contexto** usar → `docker-desktop`

```yaml
# Ejemplo simplificado de ~/.kube/config
apiVersion: v1
clusters:
- cluster:
    server: https://kubernetes.docker.internal:6443   # API server de K8s
    certificate-authority-data: LS0tLS1...             # Certificado TLS
  name: docker-desktop
contexts:
- context:
    cluster: docker-desktop
    user: docker-desktop
  name: docker-desktop
current-context: docker-desktop
users:
- name: docker-desktop
  user:
    client-certificate-data: LS0tLS1...  # Certificado del cliente
    client-key-data: LS0tLS1...          # Clave privada del cliente
```

### Por que funciona sin configuracion adicional?

Jenkins corre como un proceso Java en tu maquina local. Cuando el pipeline ejecuta `kubectl` o `helm`, estos comandos heredan el mismo kubeconfig que usas tu en la terminal:

- Si tu puedes hacer `kubectl get pods` en tu terminal → Jenkins tambien puede
- Ambos usan el mismo `~/.kube/config`
- Ambos se conectan al mismo cluster K8s de Docker Desktop

**No se necesita ningun plugin de Kubernetes en Jenkins.** La conexion es directa via CLI.

### Flujo paso a paso en el pipeline

```
Jenkins Pipeline
    │
    ├─ mvn clean verify           → Compila y testea (Java directo en la maquina)
    │
    ├─ docker build               → Construye imagenes Docker
    │   └─ usa el Docker daemon de Docker Desktop
    │
    ├─ kubectl create namespace   → Crea el namespace si no existe
    │   └─ lee ~/.kube/config → conecta al API Server (localhost:6443)
    │
    ├─ helm upgrade --install     → Despliega todos los recursos en K8s
    │   └─ helm usa kubectl internamente → misma conexion via kubeconfig
    │
    ├─ kubectl wait               → Espera que los pods esten listos
    │
    └─ bru run                    → Ejecuta tests E2E contra el API Gateway
        └─ accede via host.docker.internal:NodePort
```

### Como llegan las imagenes Docker a K8s?

En Docker Desktop, hay un detalle importante: **Docker y K8s comparten el mismo daemon**:

1. Jenkins ejecuta `docker build` → la imagen queda en el daemon local
2. K8s (que usa el mismo daemon) ya puede ver esas imagenes
3. Los deployments usan `imagePullPolicy: IfNotPresent` → no intenta descargar de un registry
4. **No necesitas Docker Hub, ECR, ni ningun registry externo**

Si usas Minikube en lugar de Docker Desktop, el daemon es diferente. Por eso el pipeline tiene un stage "Load Images to K8s" que detecta Minikube y usa `minikube image load` para copiar las imagenes al daemon de Minikube.

### Diferencia con un Jenkins remoto

| Aspecto | Jenkins local | Jenkins remoto (AWS/Cloud) |
|---|---|---|
| **kubeconfig** | Viene de Docker Desktop (automatico) | Hay que configurarlo manualmente |
| **Cluster K8s** | Docker Desktop (single-node) | EKS, GKE, o cluster propio |
| **Autenticacion** | Certificados TLS locales | ServiceAccount token o IAM role |
| **Imagenes Docker** | Daemon compartido (no registry) | Necesitas un registry (ECR, Docker Hub) |
| **Red** | Todo es localhost | VPC, security groups, firewalls |

### Resumen de componentes

| Componente | Funcion | Como se conecta |
|---|---|---|
| **Jenkins** | Orquestador del pipeline | Ejecuta CLIs directamente |
| **kubectl** | CLI para hablar con K8s | Lee `~/.kube/config` → API Server |
| **helm** | Gestor de releases K8s | Usa kubectl internamente |
| **docker** | Construye imagenes | Socket `/var/run/docker.sock` |
| **Docker Desktop K8s** | Cluster local | API en `localhost:6443` |

---

## 17. Glosario

| Termino | Definicion |
|---|---|
| **Pod** | Unidad minima en K8s. Contiene uno o mas contenedores. |
| **Deployment** | Recurso K8s que gestiona pods (replicas, rolling updates). |
| **Service** | Recurso K8s que expone pods con un DNS interno y balancea carga. |
| **Namespace** | Agrupacion logica en K8s para aislar recursos. |
| **NodePort** | Tipo de Service que expone un puerto en cada nodo del cluster. |
| **ClusterIP** | Tipo de Service solo accesible dentro del cluster (default). |
| **StatefulSet** | Como Deployment, pero para apps con estado (ej: bases de datos). |
| **HPA** | Horizontal Pod Autoscaler. Escala pods segun metricas (CPU, etc). |
| **Ingress** | Reglas HTTP para enrutar trafico externo a Services internos. |
| **Helm Chart** | Paquete de templates K8s con valores configurables. |
| **Helm Release** | Instancia de un chart desplegada en el cluster. |
| **Pipeline** | Secuencia automatizada de pasos (build, test, deploy). |
| **Stage** | Paso logico dentro de un pipeline de Jenkins. |
| **Workspace** | Directorio temporal donde Jenkins clona el repo y trabaja. |
| **Docker Socket** | Archivo `/var/run/docker.sock` que permite comunicarse con Docker. |
| **Build Context** | Archivos enviados a Docker al hacer `docker build`. |
| **Image Tag** | Version de una imagen Docker (ej: `:latest`, `:15`). |
| **Readiness Probe** | Health check que determina si un pod puede recibir trafico. |
| **Liveness Probe** | Health check que determina si un pod debe reiniciarse. |
| **Kafka** | Broker de mensajeria para eventos asincronos entre servicios. |
| **Flyway** | Herramienta de migraciones de base de datos (DDL versionado). |
| **Bruno** | Herramienta de testing de APIs basada en archivos de texto. |
| **E2E** | End-to-End. Tests que prueban el sistema completo integrado. |
| **CI/CD** | Continuous Integration / Continuous Deployment. |
| **NodePort** | Puerto (30000-32767) que K8s asigna para acceso externo. |
| **host.docker.internal** | Hostname especial para que contenedores accedan al host. |

---

## Resumen Visual: Todo junto

```
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│  DESARROLLADOR                                                           │
│      │                                                                   │
│      │ git push                                                          │
│      ▼                                                                   │
│  ┌────────┐    clone     ┌────────────────────────────────────────────┐  │
│  │ GitHub │◄────────────│            JENKINS (:9090)                 │  │
│  │  main  │─────────────►│                                            │  │
│  └────────┘              │  1. mvn clean verify (compile + test)      │  │
│                          │  2. docker build x6 (secuencial)            │  │
│                          │  3. helm upgrade --install                  │  │
│                          │  4. kubectl wait (pods ready)               │  │
│                          │  5. bru run (E2E tests)                     │  │
│                          └──────────┬─────────────────────────────────┘  │
│                                     │                                    │
│                          ┌──────────▼─────────────────────────────────┐  │
│                          │      KUBERNETES (Docker Desktop)           │  │
│                          │      Namespace: food-ordering-e2e          │  │
│                          │                                            │  │
│                          │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐│  │
│                          │  │Users│ │Order│ │Catal│ │Paym │ │Deliv││  │
│                          │  │ DB  │ │ DB  │ │ DB  │ │ DB  │ │ DB  ││  │
│                          │  └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘│  │
│                          │     │       │       │       │       │    │  │
│                          │  ┌──▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──┐│  │
│                          │  │Users│ │Order│ │Catal│ │Paym │ │Deliv││  │
│                          │  │ Svc │ │ Svc │ │ Svc │ │ Svc │ │ Svc ││  │
│                          │  └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘│  │
│                          │     └───┬───┘   ┌───┘       └───┬───┘    │  │
│                          │         │  KAFKA│                │       │  │
│                          │         └───┬───┘                │       │  │
│                          │         ┌───▼────────────────────▼──┐    │  │
│                          │         │     API GATEWAY (:8080)   │    │  │
│                          │         │     NodePort: XXXXX       │    │  │
│                          │         └───────────────────────────┘    │  │
│                          └─────────────────────────────────────────┘  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

*Documento generado para el proyecto Sistema de Pedidos de Comida*
*Stack: Java 25 + Spring Boot + Docker + Kubernetes + Helm + Jenkins + Bruno*
