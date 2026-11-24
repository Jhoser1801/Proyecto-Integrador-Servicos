# Parqueadero – Proyecto Integrador

Desarrollo Orientado a Servicios 2026-2

| Integrante | GitHub |
|---|---|
| Jhoser Daniel Briceño Higuera | [Jhoser1801](https://github.com/Jhoser1801) |
| Joseph Santiago Alvarez Higuera | [Joseph-Alvarezz](https://github.com/Joseph-Alvarezz) |
| Camilo Andres Molina Higuera | [camilomolih-ai](https://github.com/camilomolih-ai) |
| Stevan Javier Alarcon Molina | [StevanAlarcon](https://github.com/StevanAlarcon) |

Repositorio: https://github.com/Jhoser1801/Proyecto-Integrador-Servicos

---

## Descripción del proyecto

Sistema web para administrar un parqueadero de carros y motos. Registra la entrada de cada vehículo en el primer
cupo libre de su tipo, registra la salida, cobra por hora o fracción y guarda el historial de todo lo que pasó.

**Problema que resuelve:** en un parqueadero manejado a mano se pierde el control de qué cupos están libres, se
cobra mal el tiempo de permanencia y no queda registro para saber cuánto se recaudó. El sistema lleva los cupos en
tiempo real, calcula el cobro con una regla fija y genera reportes de ingresos.

**Usuarios:**

| Usuario | Qué hace |
|---|---|
| Visitante (sin cuenta) | Consulta cuántos cupos libres hay para carros y motos antes de llegar |
| Operador | Registra entradas y salidas, consulta el mapa de cupos, el historial y los comprobantes |
| Administrador | Todo lo del operador, más gestionar cupos, tarifas, usuarios, ver reportes y la base de datos |

**Funcionalidades principales:**

- Mapa de cupos por zonas (carros y motos) con la placa y la hora de entrada de cada vehículo.
- Registro de entrada con validación de placa colombiana: carro `ABC123`, moto `ABC12D`.
- Registro de salida con confirmación y cálculo automático del cobro (hora o fracción, mínimo una hora).
- Historial de tickets con filtros por placa, tipo, estado y fechas, y comprobante de cada salida.
- Administración de cupos, tarifas y usuarios (crear, editar y eliminar), con reglas que protegen el historial.
- Reporte de ingresos por rango de fechas: total, vehículos atendidos, ingresos por día y por tipo de vehículo.
- Página pública de disponibilidad, sin datos privados (no muestra placas ni horas).
- Inicio de sesión con roles; el login no tiene botón visible, se entra por la ruta `/login`.
- API REST en JSON con los mismos datos, para que otros programas (una app, una cámara de placas) usen el sistema.

---

## Pasos para correr el proyecto

### Requisitos previos

| Requisito | Versión | Nota |
|---|---|---|
| JDK | 25 | Es la que pide `build.gradle.kts`. Con JDK 21 funciona cambiando `JavaLanguageVersion.of(25)` por `of(21)` |
| Gradle | 9.6 | No hay que instalarlo: viene con el proyecto (`gradlew` / `gradlew.bat`) |
| IDE recomendado | IntelliJ IDEA | Community o Ultimate |
| Internet | — | Solo la primera vez, para descargar dependencias; y para las fuentes e iconos de Google Fonts |



### Base de datos

Se usa **H2** en modo archivo, que viene como dependencia del proyecto. No hay que instalarla ni crearla: la primera
vez que se ejecuta la aplicación se crea sola la carpeta `data/` con el archivo `parking.mv.db`, las tablas
(`setupTables()` de cada repositorio) y los datos de prueba (`DataSeed`). Los datos se conservan entre ejecuciones.

### Archivo de configuración y variables de entorno

La configuración está en `src/main/resources/application.properties`:

```properties
server.port=8080
# permite PUT/DELETE desde formularios HTML
spring.mvc.hiddenmethod.filter.enabled=true

# Base de datos H2 (el usuario y la contraseña van aquí).
app.db.url=jdbc:h2:file:./data/parking
app.db.username=parking
app.db.password=parking2026

# Consola web de H2: http://localhost:8080/h2
spring.h2.console.enabled=true
spring.h2.console.path=/h2
```

No hay que cambiar nada para ejecutarlo. Si se cambian el usuario o la contraseña de la base después de la primera
ejecución, hay que borrar la carpeta `data/` para que H2 la cree de nuevo.

Variables de entorno **opcionales** (si existen, tienen prioridad sobre el archivo):

| Variable | Valor por defecto | Para qué sirve |
|---|---|---|
| `DB_URL` | `jdbc:h2:file:./data/parking` | Ubicación de la base. Con `jdbc:h2:mem:parkingdb;DB_CLOSE_DELAY=-1` queda solo en memoria |
| `DB_USER` | `parking` | Usuario de la base |
| `DB_PASSWORD` | `parking2026` | Contraseña de la base |
| `ADMIN_USER` | `admin` | Usuario administrador que se crea la primera vez |
| `ADMIN_PASSWORD` | `admin123` | Contraseña de ese administrador |

### Credenciales de prueba y puertos

| Qué | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Operador | `operator` | `operator123` |
| Base de datos H2 (consola `/h2`) | `parking` | `parking2026` |

| Servidor | Puerto |
|---|---|
| Vistas web (Spring + Thymeleaf) | 8080 |
| API REST (Javalin) | 7070 |

### Ejecutar en IntelliJ IDEA

1. *File → Open* y elegir la carpeta del proyecto. Sí pregunta, pulsar **Trust Project**.
2. Esperar a que Gradle descargue las dependencias (barra de progreso abajo a la derecha).
3. Revisar el JDK en *File → Project Structure → Project → SDK* (JDK 25).
4. Abrir `src/main/java/dev/brice/parking/Application.java` y pulsar el triángulo verde junto a `main`.

### Ejecutar desde la terminal

```bash
# Windows
gradlew.bat build      # descarga dependencias, compila y corre las pruebas
gradlew.bat run        # ejecuta la aplicación

# Linux o Mac
./gradlew build
./gradlew run
```

Para correr solo las pruebas: `gradlew.bat test` o `./gradlew test`.

### URL una vez iniciado

| URL | Qué es |
|---|---|
| http://localhost:8080/cupos | Disponibilidad pública (también se ve al abrir http://localhost:8080 sin sesión) |
| http://localhost:8080/login | Inicio de sesión del personal |
| http://localhost:8080/ | Mapa de cupos (con sesión) |
| http://localhost:8080/h2 | Consola de la base de datos (solo administrador). JDBC URL: `jdbc:h2:file:./data/parking` |
| http://localhost:7070/api/slots | API REST (pide usuario y contraseña con HTTP Basic) |

### Errores comunes al ejecutar por primera vez

| Error | Solución |
|---|---|
| `Cannot find a Java installation ... matching: {languageVersion=25}` | Instalar JDK 25, o cambiar `JavaLanguageVersion.of(25)` por la versión instalada (21) en `build.gradle.kts` |
| `No se pudo abrir la base de datos H2...` al arrancar | La carpeta `data/` se creó con otro usuario o contraseña. Cerrar la app y borrar `data/` (se vuelve a crear con los datos de prueba) |
| `Port 8080 was already in use` o el 7070 ocupado | Ya hay otra copia corriendo. Detenerla (cuadro rojo en IntelliJ) y volver a ejecutar |
| Las letras o iconos se ven distintos | No hay internet: las fuentes vienen de Google Fonts. El sistema funciona igual |
| Se ven datos viejos o se quiere empezar de cero | Cerrar la app y borrar la carpeta `data/` |

---

## Estructura de carpetas

```
src/
├── main/
│   ├── java/dev/brice/parking/
│   │   ├── Application.java              punto de entrada: crea adaptadores y servicios y arranca los servidores
│   │   ├── DataSeed.java                 datos de prueba (solo si la base está vacía)
│   │   ├── core/                         NÚCLEO: Java puro, sin frameworks
│   │   │   ├── constant/                 VehicleType, SlotStatus, TicketStatus, UserRole
│   │   │   ├── entity/                   ParkingSlot, ParkingTicket, Tariff, User
│   │   │   ├── dto/                      CheckInDTO, SlotDTO, TariffDTO, UserDTO, UserUpdateDTO,
│   │   │   │                             ReceiptDTO, ReportDataDTO, RevenueSummaryDTO, DailyRevenueDTO
│   │   │   ├── port/                     SlotRepository, TicketRepository, TariffRepository,
│   │   │   │                             UserRepository, PasswordHasher, ReportGateway
│   │   │   └── service/                  ParkingService, SlotService, TariffService, UserService, ReportService
│   │   └── adapter/                      ADAPTADORES: conectan el núcleo con el exterior
│   │       ├── repository/
│   │       │   ├── h2/                   H2SlotRepository, H2TicketRepository, H2TariffRepository, H2UserRepository
│   │       │   │   └── dao/              SlotDAO, TicketDAO, TariffDAO, UserDAO (SQL con JDBI)
│   │       │   └── inmemory/             repositorios en memoria (para pruebas)
│   │       ├── security/                 BCryptPasswordHasher
│   │       └── ui/
│   │           ├── javalin/              JavalinUI (API REST)
│   │           └── spring/               SpringUI, SpringConfig
│   │               ├── config/           SecurityConfig (login y roles)
│   │               └── controller/       Dashboard, Ticket, Slot, Tariff, User, Report, Public y LoginController
│   └── resources/
│       ├── application.properties
│       ├── static/css/app.css            única hoja de estilos (componentes reutilizables)
│       └── templates/
│           ├── fragments/                layout.html (piezas comunes) y bays.html (zona del mapa)
│           ├── index.html                mapa de cupos
│           ├── login.html
│           ├── public/                   disponibilidad pública
│           ├── tickets/                  historial y comprobante
│           ├── slots/  tariffs/  users/  reports/
│           └── error/                    400, 403, 404, 500 y función no disponible
└── test/java/dev/brice/parking/
    ├── core/entity/                      pruebas de las entidades
    ├── core/service/                     pruebas de los servicios
    ├── adapter/repository/h2/            pruebas del SQL contra H2
    └── DataSeedTest.java
```

---

## Diagrama de arquitectura

El proyecto usa **arquitectura hexagonal (puertos y adaptadores)**:

- El **núcleo** (`core`) tiene las reglas de negocio y no depende de ningún framework.
- Los **puertos** (`core/port`) son interfaces que el núcleo define para lo que necesita del exterior: guardar
  datos y cifrar contraseñas.
- Los **adaptadores de salida** implementan esos puertos: repositorios H2 con JDBI y BCrypt.
- Los **adaptadores de entrada** usan los servicios: los controladores Spring (vistas Thymeleaf) y `JavalinUI`
  (API REST). Ambos comparten los mismos servicios.
- **`Application`** es la raíz de composición: crea cada pieza y se la pasa al núcleo por el constructor
  (inyección de dependencias manual).

Patrones usados: puertos y adaptadores, repositorio, DTO, inyección de dependencias por constructor, raíz de
composición y objetos inmutables (`record` con métodos `with...`).

![Diagrama de arquitectura](docs/diagrama-arquitectura.png)

Diagrama de clases UML, de arriba hacia abajo: los controladores Spring y `JavalinUI` (adaptadores de entrada) usan
los servicios; los servicios usan los puertos (interfaces); los adaptadores de salida (repositorios H2 y BCrypt)
implementan esos puertos. Las flechas punteadas con punta abierta son dependencias (la clase recibe a la otra por el
constructor) y las punteadas con triángulo vacío son realizaciones (el adaptador implementa la interfaz). Todas las
dependencias apuntan hacia el núcleo.

El diagrama se hizo en Miro; su código fuente (Mermaid) está en
[`docs/diagrama-arquitectura.mmd`](docs/diagrama-arquitectura.mmd).

Las entidades del núcleo (`ParkingSlot`, `ParkingTicket`, `Tariff`, `User`) son `record` inmutables que se validan
en su constructor: un ticket con placa inválida o un cupo sin código no pueden existir.

---

## Modelo Entidad/Relación

![Modelo entidad relación](docs/modelo-entidad-relacion.png)

| Tabla | Llave primaria | Restricciones |
|---|---|---|
| `parking_slot` | `slot_id` | `code` único |
| `parking_ticket` | `ticket_id` | `slot_id` es llave foránea a `parking_slot` (`fk_ticket_slot`) |
| `tariff` | `tariff_id` | `vehicle_type` único: una tarifa por tipo de vehículo |
| `app_user` | `user_id` | `username` único. Se llama `app_user` porque `user` es palabra reservada en H2 |

Un cupo puede tener muchos tickets a lo largo del tiempo, pero solo uno abierto a la vez. La tarifa se relaciona con
el ticket por el tipo de vehículo, sin llave foránea: el valor cobrado queda guardado en `total_amount`.

El diagrama se hizo en Miro; su código fuente (Mermaid) está en
[`docs/modelo-entidad-relacion.mmd`](docs/modelo-entidad-relacion.mmd).

---

## Endpoints o peticiones disponibles

### Vistas web (Spring MVC + Thymeleaf, puerto 8080)

| Funcionalidad | Ruta | Método | Descripción |
|---|---|---|---|
| Disponibilidad pública | `/cupos` | GET | Cupos libres de carros y motos, sin datos privados. Se actualiza cada 30 s |
| Inicio | `/` | GET | Sin sesión redirige a `/cupos`; con sesión muestra el mapa de cupos |
| Detalle de un cupo | `/?slot=A1` | GET | Muestra placa, tipo, hora y tiempo dentro del cupo elegido |
| Confirmar salida | `/?slot=A1&confirmar` | GET | Ventana de confirmación antes de registrar la salida |
| Iniciar sesión | `/login` | GET / POST | Formulario de login (el POST lo procesa Spring Security) |
| Cerrar sesión | `/logout` | POST | Termina la sesión |
| Registrar entrada | `/tickets` | POST | Crea el ticket y ocupa el primer cupo libre |
| Historial de tickets | `/tickets?q=&type=&status=&from=&to=` | GET | Lista con filtros por placa, tipo, estado y fechas |
| Registrar salida | `/tickets/{id}/check-out` | PUT | Cierra el ticket, calcula el cobro y libera el cupo |
| Comprobante | `/tickets/{id}` | GET | Detalle del ticket con tiempo, tarifa y total |
| Comprobante en PDF | `/tickets/{id}/receipt.pdf` | GET | Pendiente: responde "función no disponible" (501) |
| Administrar cupos | `/slots` (`?crear`, `?editar={id}`) | GET | Lista de cupos y ventanas de crear o editar |
| Crear cupo | `/slots` | POST | Nuevo cupo libre |
| Editar cupo | `/slots/{id}` | PUT | Cambia código o tipo (solo cupos libres) |
| Eliminar cupo | `/slots/{id}` | DELETE | Solo cupos libres que nunca se usaron |
| Tarifas | `/tariffs` | GET | Valor por hora de carros y motos |
| Actualizar tarifa | `/tariffs/{vehicleType}` | PUT | Nuevo valor por hora |
| Usuarios | `/users` (`?crear`, `?editar={id}`) | GET | Lista de usuarios y ventanas de crear o editar |
| Crear usuario | `/users` | POST | Nuevo operador o administrador |
| Editar usuario | `/users/{id}` | PUT | Cambia rol y, opcionalmente, contraseña |
| Eliminar usuario | `/users/{id}` | DELETE | No permite borrar la propia cuenta ni el último administrador |
| Reporte de ingresos | `/reports?from=&to=` | GET | Totales, ingresos por día y por tipo (por defecto, últimos 7 días) |
| Reporte en PDF | `/reports/revenue.pdf` | GET | Pendiente: responde "función no disponible" (501) |
| Consola H2 | `/h2` | GET | Consola de la base de datos |

`/slots`, `/tariffs`, `/users`, `/reports` y `/h2` son solo para el administrador. Las demás rutas internas piden
sesión de operador o administrador. Los formularios HTML envían PUT y DELETE con el campo oculto `_method`.

### API REST (Javalin, puerto 7070, autenticación HTTP Basic)

| Funcionalidad | Ruta | Método | Descripción |
|---|---|---|---|
| Listar cupos | `/api/slots` | GET | Todos los cupos con su estado |
| Crear cupo | `/api/slots` | POST | `{"code":"A6","vehicleType":"CAR"}` (ADMIN) |
| Editar cupo | `/api/slots/{id}` | PUT | `{"code":"A7","vehicleType":"CAR"}` (ADMIN) |
| Eliminar cupo | `/api/slots/{id}` | DELETE | Responde 204 (ADMIN) |
| Listar usuarios | `/api/users` | GET | Sin contraseñas ni hashes (ADMIN) |
| Crear usuario | `/api/users` | POST | `{"username":"oper2","password":"clave123","role":"OPERATOR"}` (ADMIN) |
| Editar usuario | `/api/users/{id}` | PUT | `{"role":"ADMIN","password":"nueva123"}` (ADMIN) |
| Eliminar usuario | `/api/users/{id}` | DELETE | Responde 204 (ADMIN) |
| Listar tarifas | `/api/tariffs` | GET | Valor por hora de cada tipo |
| Actualizar tarifa | `/api/tariffs/{vehicleType}` | PUT | `{"hourlyRate":3500}` con `CAR` o `MOTORCYCLE` (ADMIN) |
| Listar tickets | `/api/tickets` | GET | Todos los tickets, del más reciente al más antiguo |
| Tickets abiertos | `/api/tickets/open` | GET | Vehículos que están dentro |
| Consultar ticket | `/api/tickets/{id}` | GET | Un ticket (404 si no existe) |
| Registrar entrada | `/api/tickets/check-in` | POST | `{"plate":"ABC123","vehicleType":"CAR"}`, responde 201 |
| Registrar salida | `/api/tickets/{id}/check-out` | PUT | Cierra el ticket y devuelve el cobro |
| Comprobante | `/api/tickets/{id}/receipt` | GET | Datos del comprobante de un ticket cerrado |
| Reporte de ingresos | `/api/reports/revenue?from=yyyy-MM-dd&to=yyyy-MM-dd` | GET | Tickets cerrados y total del rango (ADMIN) |

Respuestas de error: **400** si se rompe una regla de negocio o el JSON es inválido (`{"error": "mensaje"}`),
**401** sin credenciales válidas, **403** si un operador usa una ruta de administrador, **404** si no existe.
Ejemplo: `curl -u admin:admin123 http://localhost:7070/api/slots`.

---

## Stack de tecnologías

**Versión de Java:** Java 25 (`toolchain` en `build.gradle.kts`). **Herramienta de construcción:** Gradle 9.6 con
Kotlin DSL.

### Plugins de Gradle

| Plugin | Versión | Para qué |
|---|---|---|
| `java`, `application` | (incluidos en Gradle) | Compilar Java y la tarea `run` con la clase principal |
| `org.springframework.boot` | 4.1.0 | Spring Boot |
| `io.spring.dependency-management` | 1.1.7 | Versiones compatibles de las dependencias de Spring |

### Dependencias de `build.gradle.kts`

| Dependencia | Versión | Para qué se usa |
|---|---|---|
| `spring-boot-starter-webmvc` | 4.1.0 (Spring MVC 7.0.8, Tomcat 11.0.22) | Servidor web y controladores |
| `spring-boot-starter-thymeleaf` | 4.1.0 (Thymeleaf 3.1.5) | Vistas HTML |
| `spring-boot-starter-security` | 4.1.0 (Spring Security 7.1.0) | Login, roles, CSRF y BCrypt |
| `thymeleaf-extras-springsecurity6` | 3.1.5.RELEASE | `sec:authorize` en las plantillas |
| `io.javalin:javalin` | 7.2.4 (Jetty 12.1.10) | API REST |
| `jackson-databind` | 2.21.4 | Conversión entre objetos Java y JSON |
| `jackson-datatype-jsr310` | 2.21.4 | Fechas (`LocalDateTime`) en JSON |
| `org.jdbi:jdbi3-core` | 3.55.0 | Acceso a la base con SQL |
| `org.jdbi:jdbi3-sqlobject` | 3.55.0 | DAOs como interfaces con `@SqlQuery` y `@SqlUpdate` |
| `com.h2database:h2` | 2.4.240 | Base de datos en archivo |
| `spring-boot-h2console` | 4.1.0 | Consola web de H2 en `/h2` |
| `spring-boot-starter-test` | 4.1.0 (JUnit Jupiter 6.0.3) | Pruebas unitarias |

### Interfaz

| Tecnología | Uso |
|---|---|
| HTML con Thymeleaf | Plantillas con fragmentos reutilizables |
| CSS propio (`app.css`) | Una sola hoja con variables y componentes reutilizables; **sin JavaScript** |
| Google Fonts | Fuentes Alata, Instrument Sans y Red Hat Mono, e iconos Material Symbols |

---

## Capturas UI/UX

Capturas del sistema en ejecución con los datos de prueba, las mismas pantallas del video.

**Disponibilidad pública** (sin iniciar sesión)

![Disponibilidad pública](docs/capturas/01-disponibilidad-publica.png)

**Inicio de sesión**

![Login](docs/capturas/02-login.png)

**Mapa de cupos**

![Mapa de cupos](docs/capturas/03-mapa-de-cupos.png)

**Registrar entrada de una moto** (la ayuda muestra el formato de placa de moto)

![Registrar entrada de moto](docs/capturas/04-registrar-entrada-moto.png)

**Detalle de un cupo ocupado**

![Detalle del cupo](docs/capturas/05-detalle-del-cupo.png)

**Confirmar salida**

![Confirmar salida](docs/capturas/06-confirmar-salida.png)

**Historial de tickets**

![Historial de tickets](docs/capturas/07-historial-de-tickets.png)

**Historial filtrado** (carros cerrados)

![Historial filtrado](docs/capturas/08-historial-filtrado.png)

**Comprobante de salida**

![Comprobante](docs/capturas/09-comprobante.png)

**Administrar cupos**

![Administrar cupos](docs/capturas/10-administrar-cupos.png)

**Crear cupo**

![Crear cupo](docs/capturas/11-crear-cupo.png)

**Tarifas**

![Tarifas](docs/capturas/12-tarifas.png)

**Usuarios**

![Usuarios](docs/capturas/13-usuarios.png)

**Crear usuario**

![Crear usuario](docs/capturas/14-crear-usuario.png)

**Reporte de ingresos**

![Reportes](docs/capturas/15-reportes.png)

**Página no encontrada (404)**

![Error 404](docs/capturas/16-error-404.png)

**Operador intentando entrar a administración (403)**

![Error 403](docs/capturas/17-error-403-operador.png)

**Disponibilidad en el celular**

![Disponibilidad en celular](docs/capturas/18-celular-disponibilidad.png)

---

## Análisis personal

**Retos más grandes**

- Lo más difícil fue entender cómo organizar el proyecto por capas. Al principio no teníamos claro qué parte era
  la lógica del parqueadero y qué parte era la página o la base de datos. Lo cambiamos varias veces hasta que
  quedó ordenado.
- Pasar de guardar los datos en memoria a guardarlos en una base de datos de verdad. Queríamos que la información
  no se perdiera al cerrar el programa, y lo logramos sin tener que cambiar las reglas que ya funcionaban.
- Preparar los computadores. Algunas herramientas no nos funcionaron en Windows, así que buscamos una opción más
  sencilla que corriera sin instalar nada extra.
- Lograr que desde la página se pudieran editar y eliminar cupos, tarifas y usuarios, y que cada persona solo viera
  lo que le corresponde según su rol (administrador u operador).
- Trabajar los cuatro sobre el mismo código. Aprendimos a subir y bajar cambios con Git sin dañar el trabajo de los
  demás, y a resolver los conflictos cuando dos tocábamos lo mismo.

**Aprendizajes más valiosos**

- Tener la lógica del negocio separada de lo demás vale la pena: la página web y la API usan las mismas reglas, y si
  mañana cambiamos la base de datos, el resto sigue igual.
- Validar los datos desde el principio evita muchos errores. Por ejemplo, el sistema no deja registrar una placa mal
  escrita ni un vehículo que ya está adentro.
- Se puede hacer una interfaz clara y agradable reutilizando los mismos estilos en todas las pantallas, sin
  complicarse con código extra.
- La seguridad no es opcional: las contraseñas se guardan cifradas y cada pantalla revisa quién está entrando.
- Hacer pruebas nos dio tranquilidad. Cada vez que cambiábamos algo, las pruebas nos avisaban si habíamos dañado
  otra parte.

---

## Pendiente

- Reportes y comprobantes en PDF: `ReportService` ya arma los datos y el puerto `ReportGateway` está reservado;
  falta el adaptador que genere el PDF. Mientras tanto, los botones de PDF muestran "función no disponible".
