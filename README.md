# Parqueadero – Proyecto Integrador (Desarrollo Orientado a Servicios 2026-2)

**Problema:** un parqueadero no controla los cupos ni cobra bien las horas de permanencia.
**Usuarios:** operador (registra entradas y salidas) y administrador (cupos, tarifas y reportes).
**Datos:** cupos, tarifas por tipo de vehículo, tickets de entrada/salida y usuarios.

## Arquitectura hexagonal

```
src/main/java/dev/brice/parking/
├── Application.java          raíz de composición: elige adapters y arma los casos de uso
├── DataSeed.java             datos de prueba (solo se siembran si la base está vacía)
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

### Rutas

El sistema **no tiene ningún botón de login a la vista**: al personal se le da la ruta y entra escribiéndola.

| Ruta | Quién entra | Qué es |
|---|---|---|
| `/cupos` | Cualquier persona, sin sesión | Cantidad de cupos libres (no muestra placas, horas ni códigos de cupo). Es también lo que se ve al abrir `/` sin sesión |
| `/login` | Personal (se escribe la ruta) | Inicio de sesión. Es la única forma de llegar a las pantallas internas |
| `/` | Con sesión | Mapa de cupos y registro de entradas |
| `/h2` | Solo el administrador | Consola web de H2 para ver y consultar las tablas |

### Consola web de H2 (`/h2`)

Como en `business-sales-project`, la base se puede mirar desde el navegador. Con sesión de administrador, abrir `http://localhost:8080/h2` e ingresar con:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/parking` |
| User name | `parking` |
| Password | `parking2026` |

Se ven las tablas `PARKING_SLOT`, `PARKING_TICKET`, `TARIFF` y `APP_USER` y se pueden hacer consultas, por ejemplo `select * from parking_ticket`. La consola solo la abre el administrador con sesión iniciada, y H2 solo acepta conexiones desde el mismo equipo.


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

## Base de datos

H2 con JDBI, guardada en un archivo: `jdbc:h2:file:./data/parking` (con usuario y contraseña propios: `parking` / `parking2026`, definidos en `application.properties`). La primera vez que se ejecuta se crea la carpeta `data/` con `parking.mv.db`, las tablas se crean solas (`setupTables()` de cada repositorio) y `DataSeed` carga los datos de prueba. En los arranques siguientes la base ya existe: los datos registrados se conservan y `DataSeed` no se repite (tiene una guarda al inicio: si ya hay usuarios, no siembra).

- **Empezar de cero:** cerrar la aplicación y borrar la carpeta `data/`.
- **Una sola aplicación a la vez:** no se debe ejecutar dos copias con la misma carpeta `data/` (los puertos 8080 y 7070 tampoco lo permiten).
- **Dónde queda `data/`:** en la carpeta desde la que se ejecuta el proyecto. Con `gradlew bootRun` o con el botón verde de IntelliJ es la carpeta raíz del proyecto.
- **Base temporal en memoria:** definir la variable de entorno `DB_URL` con `jdbc:h2:mem:parkingdb;DB_CLOSE_DELAY=-1` (en IntelliJ: *Run > Edit Configurations > Environment variables*).
- **Sin base de datos:** cambiar los `new H2...Repository(jdbi)` por `new InMemory...Repository()`.

### Usuario y contraseña de la base

Como en `business-sales-project`, el usuario y la contraseña de la base están en `src/main/resources/application.properties`:

```properties
app.db.url=jdbc:h2:file:./data/parking
app.db.username=parking
app.db.password=parking2026
```

- H2 crea la base **la primera vez** con ese usuario y contraseña; después hay que usar los mismos. Si se cambian cuando la carpeta `data/` ya existe, la aplicación no arranca y avisa con un mensaje claro: se soluciona borrando la carpeta `data/` (se vuelve a crear con los datos de prueba).
- La consola `/h2` pide ese mismo usuario y contraseña; con `sa` y contraseña vacía ya no entra.

### Variables de entorno (opcionales)

Reemplazan lo que dice `application.properties` sin tocar el código (la variable tiene prioridad). Si no se definen, se usan los valores de `application.properties`:

| Variable | Valor por defecto | Para qué sirve |
|---|---|---|
| `DB_URL` | `jdbc:h2:file:./data/parking` | Dónde está la base |
| `DB_USER` | `parking` (`app.db.username`) | Usuario de la base |
| `DB_PASSWORD` | `parking2026` (`app.db.password`) | Contraseña de la base |
| `ADMIN_USER` | `admin` | Usuario del administrador que crea `DataSeed` (solo la primera vez) |
| `ADMIN_PASSWORD` | `admin123` | Contraseña de ese administrador |

## Pendiente

- Reportes en PDF: `ReportService` ya arma los datos (`ReportDataDTO`, `ReceiptDTO`); falta el adaptador que los convierta en PDF a través de `ReportGateway`. Mientras tanto las vistas muestran "función no disponible".
