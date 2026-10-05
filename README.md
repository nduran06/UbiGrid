# UbiGrid

UbiGrid es el `traffic-analysis-service` de un sistema de smart city: analiza tráfico, coordina rutas de vehículos y expone predicciones de ML. Es una app Spring Boot (Java 21) que depende de MongoDB, Redis, PostgreSQL y de servicios de AWS (hoy, SageMaker Runtime para inferencia de tráfico).

## Arquitectura local

Todo corre en contenedores sobre una misma red Docker, sin necesitar una cuenta de AWS real:

| Servicio | Imagen | Rol |
|---|---|---|
| `ubigrid` | build local (`ubigrid/Dockerfile`) | La app Spring Boot, puerto 8080 |
| `floci` | `floci/floci:latest` | Emulador local de AWS (reemplazo de LocalStack): SQS, SNS, S3, DynamoDB, CloudWatch y SageMaker (control plane + `InvokeEndpoint`), puerto 4566 |
| `mongodb` | `mongo:7.0` | Persistencia de dominio, puerto 27017 |
| `redis` | `redis:7-alpine` | Cache (`@EnableCaching`), puerto 6379 |
| `postgres` | `postgres:16-alpine` | JPA/Hibernate, puerto 5432 |

`ubigrid` depende de que los otros cuatro reporten `healthy` antes de arrancar, y usa Floci en vez de AWS real: el cliente de SageMaker Runtime (`AwsSageMakerConfig`) apunta a `http://floci:4566` con credenciales dummy. Cualquier cliente AWS que se agregue después (SQS, SNS, S3, DynamoDB, CloudWatch) resuelve contra el mismo emulador sin cambiar código, solo la configuración de endpoint.

## Levantar todo

Requiere Docker y Docker Compose (`docker compose`, no el binario viejo `docker-compose`). No hace falta tener Java ni Maven instalados: la imagen se compila dentro de Docker.

```bash
./start.sh
```

El script construye la imagen de `ubigrid`, levanta los cinco contenedores y espera a que todos reporten `healthy` antes de terminar. Al finalizar imprime las URLs de cada servicio.

```
UbiGrid API       http://localhost:8080
Actuator health   http://localhost:8080/actuator/health
Swagger UI        http://localhost:8080/swagger-ui.html
Floci (AWS)       http://localhost:4566
MongoDB           localhost:27017
Redis             localhost:6379
PostgreSQL        localhost:5432
```

Para apagar todo:

```bash
docker compose -f config/docker-compose.yml down
```

Para además borrar los datos persistidos (Mongo, Postgres, estado de Floci):

```bash
docker compose -f config/docker-compose.yml down -v
```

## Perfiles de Spring

La app tiene tres perfiles, cada uno para un modo distinto de correrla:

| Perfil | Cuándo usarlo | Cómo resuelve sus dependencias |
|---|---|---|
| (ninguno / default) | Referencia de configuración, no pensado para correr tal cual | Placeholders (`your_username`, etc.) y AWS real |
| `local` | Correr la app en el host (`./mvnw spring-boot:run`) contra los contenedores de `./start.sh` | `localhost` para Mongo/Redis/Postgres, `http://localhost:4566` para AWS/Floci |
| `docker` | La app corriendo como contenedor (el que arma `start.sh`) | Nombres de servicio Docker (`mongodb`, `redis`, `postgres`, `floci`) |

Para el perfil `local`, con la infraestructura ya arriba (`./start.sh`, o solo `docker compose -f config/docker-compose.yml up -d floci mongodb redis postgres`):

```bash
cd ubigrid
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Desarrollo sin Docker

Para compilar o testear el módulo Java directamente:

```bash
cd ubigrid
./mvnw clean test
```

Requiere JDK 21. `spring-boot-starter-parent` y el resto de dependencias se resuelven desde Maven Central la primera vez.

## Troubleshooting

- **`ubigrid-app` no llega a `healthy`**: revisa sus logs con `docker compose -f config/docker-compose.yml logs ubigrid`. Lo más común es que Postgres/Mongo/Redis tarden más de lo esperado en el primer arranque (imágenes recién descargadas); `start.sh` reintenta hasta 3 minutos por contenedor.
- **Cambios de código no se reflejan**: `start.sh` no reconstruye la imagen si no hay cambios detectados por Docker; fuerza el rebuild con `docker compose -f config/docker-compose.yml up -d --build`.
- **Puerto ocupado**: los puertos 8080, 4566, 27017, 6379 y 5432 deben estar libres en el host. Si alguno choca con otro servicio local, cambia el mapeo en `config/docker-compose.yml` (por ejemplo `"18080:8080"`).
