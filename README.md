# UbiGrid

UbiGrid es el `traffic-analysis-service` de un sistema de smart city: analiza tráfico, coordina rutas de vehículos y expone predicciones de ML. Es una app Spring Boot (Java 21) que depende de MongoDB, Redis, PostgreSQL y de servicios de AWS (hoy, SageMaker Runtime para inferencia de tráfico), y sirve un [front-end](#front-end) propio para solicitar y seguir un vehículo en el mapa.

## Arquitectura local

Todo corre en contenedores sobre una misma red Docker, sin necesitar una cuenta de AWS real:

| Servicio | Imagen | Rol |
|---|---|---|
| `ubigrid` | build local (`ubigrid/Dockerfile`) | La app Spring Boot, puerto 8080 |
| `floci` | `floci/floci:latest` | Emulador local de AWS (reemplazo de LocalStack): SQS, SNS, S3, DynamoDB, CloudWatch y SageMaker (control plane + `InvokeEndpoint`), puerto 4566 |
| `mongodb` | `mongo:7.0` | Persistencia de dominio, puerto 27017 |
| `redis` | `redis:7-alpine` | Cache (`@EnableCaching`), puerto 6380 en el host |
| `postgres` | `postgres:16-alpine` | JPA/Hibernate, puerto 5435 en el host |

`ubigrid` depende de que los otros cuatro reporten `healthy` antes de arrancar, y usa Floci en vez de AWS real: el cliente de SageMaker Runtime (`AwsSageMakerConfig`) apunta a `http://floci:4566` con credenciales dummy. Cualquier cliente AWS que se agregue después (SQS, SNS, S3, DynamoDB, CloudWatch) resuelve contra el mismo emulador sin cambiar código, solo la configuración de endpoint.

## Levantar todo

Requiere Docker y Docker Compose (`docker compose`, no el binario viejo `docker-compose`). No hace falta tener Java ni Maven instalados: la imagen se compila dentro de Docker.

```bash
./start.sh
```

El script construye la imagen de `ubigrid` (API + front-end estático, ver [Front-end](#front-end)), levanta los cinco contenedores y espera a que todos reporten `healthy` antes de terminar. Al finalizar imprime las URLs de cada servicio.

```
Front-end
  Landing            http://localhost:8080/
  Registro           http://localhost:8080/register.html
  Solicitar vehículo http://localhost:8080/request.html

API y dependencias
  UbiGrid API       http://localhost:8080
  Actuator health   http://localhost:8080/actuator/health
  Swagger UI        http://localhost:8080/swagger-ui.html
  Floci (AWS)       http://localhost:4566
  MongoDB           localhost:27017
  Redis             localhost:6380
  PostgreSQL        localhost:5435
```

Para apagar todo:

```bash
docker compose -f config/docker-compose.yml down
```

Para además borrar los datos persistidos (Mongo, Postgres, estado de Floci):

```bash
docker compose -f config/docker-compose.yml down -v
```

## Redesplegar tras cambios

Después de editar el front-end (`static/`) o el código Java, no hace falta reiniciar todo el stack:

```bash
./redeploy.sh                 # enlace de autologin con natalia@mail.com
./redeploy.sh otro@mail.com   # enlace de autologin con otro correo
```

Reconstruye solo la imagen de `ubigrid`, recrea únicamente `ubigrid-app` (Floci, Mongo, Redis y Postgres no se tocan), espera a que esté `healthy` y al final imprime el enlace `request.html?autologin=<correo>`.

## Front-end

`ubigrid/src/main/resources/static/` tiene un front-end estático (HTML/CSS/JS, sin build step) que Spring Boot sirve directamente en la raíz de la app. Sigue el mismo patrón que [Wild Rydes](https://github.com/nduran06/AWS-wildrydes-site), el taller serverless de AWS, adaptado al dominio de UbiGrid: en vez de pedir un unicornio, pides un vehículo y lo ves llegar en el mapa.

| Página | Rol |
|---|---|
| `/` (`index.html`) | Landing: qué es UbiGrid y cómo funciona |
| `/register.html`, `/verify.html`, `/signin.html` | Alta de cuenta, verificación y login |
| `/request.html` | Mapa (Leaflet + OpenStreetMap): fija un punto de recogida, solicita un vehículo y observa cómo se anima hacia ese punto |

UbiGrid todavía no tiene un proveedor de autenticación real (Cognito o similar), así que ese único punto sigue simulado en el navegador. La ruta que se ve en el mapa, en cambio, la calcula el backend de verdad:

- **Autenticación** (`js/auth.js`): registro, verificación y login se guardan en `localStorage`. No hay backend detrás. Cuando UbiGrid tenga un endpoint de auth real, este módulo es el único punto de cambio.
- **Criaturas míticas**: el panel del mapa tiene un selector "Criatura mítica" que define el icono con el que se anima el vehículo asignado, sin importar su tipo (`BUS`, `CAR`, `TRUCK`, `EMERGENCY`). Opciones: 🦄 Unicornio (por defecto), 🐎 Pegaso, 🐉 Dragón, 🦅 Grifo y 🦎 Basilisco. La elección se guarda en `localStorage` (`ubigrid_creature`). Para añadir otra, agrega una entrada a `CREATURES` en `js/request.js`.
- **Autologin**: abre `/request.html?autologin=<correo>` (por ejemplo `natalia@mail.com`) y `requireSession()` crea la cuenta verificada si no existe, con contraseña aleatoria, y entra con ella. Sin ese parámetro ni sesión previa, redirige a `/signin.html`. El panel del mapa muestra el correo de la sesión activa bajo "Cuenta".
- **Solicitud de vehículo** (`js/request.js` + `POST /api/routes`): al fijar un punto de recogida, el front-end llama al backend, que asigna el vehículo sembrado más cercano y calcula su ruta con `VehicleRouteService` (distancia, duración y nivel de tráfico reales, no inventados en el navegador). El mapa dibuja esa ruta como polilínea y anima el vehículo siguiéndola, no en línea recta.

### Cómo se calcula la ruta (`POST /api/routes`)

`RouteController` (`ubigrid/.../controller/RouteController.java`) recibe `{ destinationLatitude, destinationLongitude, routePreference }`, busca —por distancia Haversine— el vehículo sembrado más cercano al punto de recogida, y le pide a `VehicleRouteService.computeOptimalRoute(...)` la ruta desde la ubicación actual de ese vehículo hasta el punto pedido. La respuesta trae el `path` (waypoints lat/lng en orden de recorrido), distancia y duración totales, y el peor nivel de tráfico entre los tramos.

Esa ruta no necesita datos de red vial (`RoadSegment`/`RoadConnection` siguen vacíos): `VehicleRouteService` genera waypoints entre origen y destino y consulta `TrafficService` —una simulación en memoria, sin datos externos— para el tráfico, tipo de vía y peajes de cada tramo. Lo único que sí hace falta es que exista al menos un `Vehicle` con ubicación conocida, y de eso se encarga `VehicleFleetSeeder`: siembra 4 vehículos alrededor de Bogotá en el primer arranque (colección `vehicles` vacía), sin tocar nada si ya hay datos.

Al estar en `static/`, el front-end no necesita servidor propio ni configuración adicional: cualquier perfil (`local`, `docker`, o corriendo la app a secas) lo sirve automáticamente en `http://localhost:8080/`.

## Datos de tráfico simulados

Todos los datos de tráfico son falsos, pero la app los trata como si llegaran de una red de sensores de la ciudad:

- **Feed** (`TrafficFeedSimulator`): 14 sensores en corredores de Bogotá (Autopista Norte, Av. Caracas, NQS, Calle 26...) publican cada 10 s una lectura con `vehicleCount`, `averageSpeed` y `congestionLevel`. La congestión sigue un perfil de hora pico (mañana y tarde, hora de Bogotá), con ruido por sensor, corredores que siempre están más cargados e incidentes ocasionales (congestión ≥ 0.92 durante ~80 s). Cada tanda se guarda en la colección `traffic_data` de MongoDB y se registra en el log (`Traffic feed: received 14 sensor readings...`).
- **Uso en el cálculo** (`TrafficServiceImpl`, `VehicleRouteServiceImpl`): la velocidad y el nivel de tráfico de cada segmento salen de las últimas lecturas, interpoladas por distancia inversa. El router arma 5 rutas candidatas (la directa y desvíos por cada lado, normal y amplio), calcula la duración de cada una con esos datos y elige la mejor según la preferencia (`SHORTEST` compara distancia; `AVOID_TOLLS` y `AVOID_HIGHWAYS` penalizan peajes y autopistas).
- **En el front**: los sensores aparecen como círculos verdes, naranjas o rojos que se actualizan cada 5 s (pasa el cursor para ver velocidad y vehículos). Al solicitar un vehículo, el panel lista las rutas evaluadas con su duración y marca la elegida, y la ruta se dibuja por tramos coloreados según el tráfico.
- **API**: `GET /api/traffic/sensors` devuelve las últimas lecturas; `POST /api/routes` incluye `segmentTraffic`, `evaluations` y `activeSensors`.
- **Configuración**: `ubigrid.traffic.feed.enabled` (por defecto `true`) y `ubigrid.traffic.feed.interval-seconds` (por defecto `10`). Con `enabled=false` la lectura queda estática y no se publica nada a MongoDB (así lo usan los tests).

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
- **Cambios de código no se reflejan**: usa `./redeploy.sh`, que fuerza el rebuild de la imagen de la app y recrea su contenedor.
- **Puerto ocupado**: los puertos 8080, 4566, 27017, 6379 y 5432 deben estar libres en el host. Si alguno choca con otro servicio local, cambia el mapeo en `config/docker-compose.yml` (por ejemplo `"18080:8080"`).
