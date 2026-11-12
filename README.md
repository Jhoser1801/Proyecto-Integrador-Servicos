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
│   ├── port/                 puertos (interfaces): repositorios, PasswordHasher, ReportGateway
│   └── service/              casos de uso con las reglas de negocio (los adapters los usan directo)
└── adapter/                  BORDE (depende del core, nunca al revés)
    ├── repository/inmemory/  persistencia temporal en memoria
    ├── security/             BCryptPasswordHasher
    ├── report/               PendingReportGateway (el PDF real se implementa después)
    └── web/
        ├── rest/             API REST con Javalin
        └── view/             vistas con Spring + Thymeleaf + Spring Security
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
| GET | `/api/tickets/{id}/receipt`, `/api/reports/revenue?from=yyyy-MM-dd&to=yyyy-MM-dd` | cualquiera / ADMIN |

## Pendiente

- Persistencia con JDBI: crear `adapter/repository/jdbi/*` implementando los puertos de `core/port` y cambiarlos en `Application`.
- Reporte PDF con OpenPDF: crear `OpenPdfReportGateway implements ReportGateway` y reemplazar `PendingReportGateway`.
  Los botones de descarga ya existen en las vistas.
