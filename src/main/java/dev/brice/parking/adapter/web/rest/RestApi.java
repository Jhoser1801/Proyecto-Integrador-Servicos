package dev.brice.parking.adapter.web.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.port.in.ParkingUseCase;
import dev.brice.parking.core.port.in.ReportUseCase;
import dev.brice.parking.core.port.in.SlotUseCase;
import dev.brice.parking.core.port.in.TariffUseCase;
import dev.brice.parking.core.port.in.UserUseCase;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.ForbiddenResponse;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

// Adaptador de entrada REST (Javalin). Solo traduce HTTP <-> casos de uso; las reglas viven en core.
// Autenticacion: HTTP Basic con los mismos usuarios de las vistas.
public class RestApi {
    private static final String USER_ATTRIBUTE = "user";

    private final ParkingUseCase parking;
    private final SlotUseCase slots;
    private final TariffUseCase tariffs;
    private final ReportUseCase reports;
    private final UserUseCase users;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS) // no exponer isFree()/isOpen() como campos
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public RestApi(ParkingUseCase parking, SlotUseCase slots, TariffUseCase tariffs,
                   ReportUseCase reports, UserUseCase users) {
        this.parking = parking;
        this.slots = slots;
        this.tariffs = tariffs;
        this.reports = reports;
        this.users = users;
    }

    public Javalin start(int port) {
        return Javalin.create(config -> {
            config.routes.before("/api/*", this::authenticate);

            config.routes.get("/api/slots", ctx -> respond(ctx, slots.findAllSlots()));
            config.routes.post("/api/slots", ctx -> {
                requireAdmin(ctx);
                ctx.status(HttpStatus.CREATED);
                respond(ctx, slots.createSlot(body(ctx, SlotDTO.class)));
            });

            config.routes.get("/api/tariffs", ctx -> respond(ctx, tariffs.findAllTariffs()));
            config.routes.put("/api/tariffs", ctx -> {
                requireAdmin(ctx);
                respond(ctx, tariffs.setTariff(body(ctx, TariffDTO.class)));
            });

            config.routes.get("/api/tickets", ctx -> respond(ctx, parking.findAllTickets()));
            config.routes.get("/api/tickets/open", ctx -> respond(ctx, parking.findOpenTickets()));
            config.routes.get("/api/tickets/{id}", ctx -> parking.findTicketById(id(ctx))
                    .ifPresentOrElse(ticket -> respond(ctx, ticket), () -> notFound(ctx)));
            config.routes.post("/api/tickets/check-in", ctx -> {
                ctx.status(HttpStatus.CREATED);
                respond(ctx, parking.checkIn(body(ctx, CheckInDTO.class)));
            });
            config.routes.post("/api/tickets/{id}/check-out", ctx -> respond(ctx, parking.checkOut(id(ctx))));

            config.routes.get("/api/tickets/{id}/receipt", ctx -> pdf(ctx, "receipt.pdf", reports.generateReceipt(id(ctx))));
            config.routes.get("/api/reports/revenue", ctx -> requireAdminThen(ctx, () -> pdf(ctx, "revenue.pdf",
                    reports.generateRevenueReport(date(ctx, "from"), date(ctx, "to")))));

            config.routes.exception(IllegalArgumentException.class, (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, e.getMessage()));
            config.routes.exception(UnsupportedOperationException.class, (e, ctx) -> error(ctx, HttpStatus.NOT_IMPLEMENTED, e.getMessage()));
            config.routes.exception(com.fasterxml.jackson.core.JsonProcessingException.class,
                    (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido"));
        }).start(port);
    }

    private void authenticate(Context ctx) {
        var credentials = ctx.basicAuthCredentials();
        if (credentials == null) {
            ctx.header("WWW-Authenticate", "Basic realm=\"parking\"");
            throw new UnauthorizedResponse("Credenciales requeridas");
        }
        User user = users.authenticate(credentials.getUsername(), credentials.getPassword())
                .orElseThrow(() -> new UnauthorizedResponse("Usuario o contrasena incorrectos"));
        ctx.attribute(USER_ATTRIBUTE, user);
    }

    private void requireAdmin(Context ctx) {
        User user = ctx.attribute(USER_ATTRIBUTE);
        if (user == null || user.role() != UserRole.ADMIN) {
            throw new ForbiddenResponse("Solo el administrador puede hacer esto");
        }
    }

    private void requireAdminThen(Context ctx, Runnable action) {
        requireAdmin(ctx);
        action.run();
    }

    private <T> T body(Context ctx, Class<T> type) throws com.fasterxml.jackson.core.JsonProcessingException {
        return json.readValue(ctx.body(), type);
    }

    private void respond(Context ctx, Object value) {
        try {
            ctx.contentType("application/json").result(json.writeValueAsString(value));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void pdf(Context ctx, String filename, byte[] content) {
        ctx.contentType("application/pdf")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .result(content);
    }

    private void notFound(Context ctx) {
        error(ctx, HttpStatus.NOT_FOUND, "No encontrado");
    }

    private void error(Context ctx, HttpStatus status, String message) {
        ctx.status(status);
        respond(ctx, Map.of("error", message == null ? status.getMessage() : message));
    }

    private UUID id(Context ctx) {
        try {
            return UUID.fromString(ctx.pathParam("id"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El id no es valido");
        }
    }

    private LocalDate date(Context ctx, String name) {
        String value = ctx.queryParam(name);
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha '" + name + "' debe tener formato yyyy-MM-dd");
        }
    }
}
