# Parqueadero – Proyecto Integrador (Desarrollo Orientado a Servicios 2026-2)

**Problema:** un parqueadero no controla los cupos ni cobra bien las horas de permanencia.
**Usuarios:** operador (registra entradas y salidas) y administrador (cupos, tarifas y reportes).
**Datos:** cupos, tarifas por tipo de vehículo, tickets de entrada/salida y usuarios.

## Arquitectura hexagonal

```
src/main/java/dev/brice/parking/
├── Application.java          raíz de composición: elige adapters y arma los casos de uso
├── SeedData.java             datos de prueba
├── core/                     DOMINIO (Java puro, sin framework)
│   ├── constant/             enums (VehicleType, SlotStatus, TicketStatus, UserRole)
│   ├── entity/               ParkingSlot, ParkingTicket, Tariff, User (records con validación)
│   ├── dto/                  datos de entrada/salida de los casos de uso
│   ├── port/                 puertos (interfaces): repositorios, PasswordHasher (ReportGateway reservado, vacío)
│   └── service/              casos de uso con las reglas de negocio (los adapters los usan directo)
└── adapter/                  BORDE (depende del core, nunca al revés)
    ├── repository/h2/        persistencia con JDBI + H2 en archivo (H2XRepository + dao/)
    ├── repository/inmemory/  persistencia en memoria (alternativa, sirve para pruebas)
    ├── security/             BCryptPasswordHasher
    └── ui/
        ├── javalin/          JavalinUI: API REST con Javalin
        └── spring/           SpringUI + SpringConfig + controller/: vistas con Spring + Thymeleaf + login
```

Reglas de negocio principales (en `core`): un vehículo no puede entrar dos veces, un cupo ocupado no se
asigna, la salida cierra el ticket y libera el cupo, y el cobro es por hora o fracción (mínimo una hora).

## Ejecutar

Requiere Java 21.

```bash
./gradlew bootRun     # vistas en http://localhost:8080  ·  REST en http://localhost:7070/api
./gradlew test        # pruebas unitarias de entidades y services
```

Usuarios de prueba:

| Usuario    | Contraseña    | Rol           |
|------------|---------------|---------------|
| `admin`    | `admin123`    | Administrador |
| `operator` | `operator123` | Operador      |

Cualquier persona, sin iniciar sesión, puede ver qué cupos están libres en `http://localhost:8080/cupos` (solo la cantidad de cupos libres; no muestra placas, horas ni códigos de cupo).

El operador registra entradas/salidas y ve tickets; el administrador además gestiona cupos, tarifas y reportes.

## API REST (HTTP Basic con los mismos usuarios)

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/slots`, `/api/tariffs`, `/api/tickets`, `/api/tickets/open`, `/api/tickets/{id}` | cualquiera |
| POST | `/api/slots` `{"code":"A9","vehicleType":"CAR"}` | ADMIN |
| PUT | `/api/tariffs` `{"vehicleType":"CAR","hourlyRate":3000}` | ADMIN |
| POST | `/api/tickets/check-in` `{"plate":"ABC123","vehicleType":"CAR"}` | cualquiera |
| POST | `/api/tickets/{id}/check-out` | cualquiera |
| GET | `/api/tickets/{id}/receipt` (JSON, ticket cerrado) | cualquiera |
| GET | `/api/reports/revenue?from=yyyy-MM-dd&to=yyyy-MM-dd` (JSON) | ADMIN |

## Docker

El proyecto no incluye archivos de Docker: se usa la imagen ya publicada en Docker Hub (requiere Docker Desktop encendido).

```bash
docker run -d --name parking -p 8080:8080 -p 7070:7070 -v parking-data:/app/data TU_USUARIO/parking-integrator:1.0
```

- Imagen: https://hub.docker.com/r/TU_USUARIO/parking-integrator
- Vistas: http://localhost:8080 · REST: http://localhost:7070/api · usuarios: `admin` / `admin123` y `operator` / `operator123`
- La base de datos es **H2 en archivo**, y vive dentro del contenedor en `/app/data/parking.mv.db`. El volumen `parking-data` hace que los datos se conserven aunque el contenedor se detenga o se borre.
- Detener: `docker stop parking` · volver a iniciar: `docker start parking`
- Borrar el contenedor: `docker rm -f parking` (los datos siguen en el volumen)
- Empezar de cero con la base vacía: `docker rm -f parking` y `docker volume rm parking-data`
- Docker no reemplaza a H2: H2 sigue corriendo dentro de la aplicación; Docker solo empaqueta la aplicación y guarda su carpeta `data/`.

## Base de datos

H2 con JDBI, guardada en un archivo: `jdbc:h2:file:./data/parking` (usuario `sa`, sin clave). La primera vez que se ejecuta se crea la carpeta `data/` con `parking.mv.db`, las tablas se crean solas (`setupTables()` de cada repositorio) y `SeedData` carga los datos de prueba. En los arranques siguientes la base ya existe: los datos registrados se conservan y `SeedData` no se repite.

- **Empezar de cero:** cerrar la aplicación y borrar la carpeta `data/`.
- **Una sola aplicación a la vez:** no se debe ejecutar dos copias con la misma carpeta `data/` (los puertos 8080 y 7070 tampoco lo permiten).
- **Dónde queda `data/`:** en la carpeta desde la que se ejecuta el proyecto. Con `gradlew bootRun` o con el botón verde de IntelliJ es la carpeta raíz del proyecto.
- **Base temporal en memoria:** cambiar `DB_URL` en `Application` por `jdbc:h2:mem:parkingdb;DB_CLOSE_DELAY=-1`.
- **Sin base de datos:** cambiar los `new H2...Repository(jdbi)` por `new InMemory...Repository()`.

## Pendiente

- Reportes en PDF: `ReportService` ya arma los datos (`ReportDataDTO`, `ReceiptDTO`); falta el adaptador que los convierta en PDF a través de `ReportGateway`. Mientras tanto las vistas muestran "función no disponible".
