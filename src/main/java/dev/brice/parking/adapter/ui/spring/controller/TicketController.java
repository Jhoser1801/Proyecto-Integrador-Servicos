package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final ParkingService parking;
    private final SlotService slots;
    private final TariffService tariffs;

    public TicketController(ParkingService parking, SlotService slots, TariffService tariffs) {
        this.parking = parking;
        this.slots = slots;
        this.tariffs = tariffs;
    }

    // Filtros por GET (?q=ABC&type=CAR&status=OPEN&from=2026-11-01&to=2026-11-22): el servidor filtra, sin JavaScript
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) VehicleType type,
                       @RequestParam(required = false) TicketStatus status,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        List<ParkingTicket> all = parking.findAllTickets();
        List<ParkingTicket> shown;
        try {
            shown = parking.searchTickets(q, type, status, from, to);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            shown = all;
        }
        model.addAttribute("tickets", shown);
        model.addAttribute("filtered", q != null && !q.isBlank() || type != null || status != null || from != null || to != null);
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("statuses", TicketStatus.values());

        model.addAttribute("totalTickets", all.size());
        model.addAttribute("openTickets", all.stream().filter(ParkingTicket::isOpen).count());
        model.addAttribute("closedTickets", all.stream().filter(t -> !t.isOpen()).count());
        model.addAttribute("totalRevenue", all.stream().filter(t -> !t.isOpen())
                .map(ParkingTicket::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return "tickets/list";
    }

    // El formulario de entrada vive en el inicio (mapa de cupos); aqui solo se recibe y se vuelve a ese inicio
    @PostMapping
    public String checkIn(@ModelAttribute CheckInDTO form, RedirectAttributes redirect) {
        try {
            ParkingTicket ticket = parking.checkIn(form);
            String slotCode = slots.findSlotById(ticket.slotId()).map(ParkingSlot::code).orElse("-");
            redirect.addFlashAttribute("success", "Entrada registrada: " + ticket.plate() + " en el cupo " + slotCode);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    // PUT: actualiza el ticket a cerrado (salida del vehiculo)
    @PutMapping("/{id}/check-out")
    public String checkOut(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            parking.checkOut(id);
            return "redirect:/tickets/" + id;
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        ParkingTicket ticket = parking.findTicketById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("ticket", ticket);
        model.addAttribute("slot", slots.findSlotById(ticket.slotId()).orElse(null));
        tariffs.findTariffByVehicleType(ticket.vehicleType()).ifPresent(tariff -> model.addAttribute("hourlyRate", tariff.hourlyRate()));
        if (!ticket.isOpen()) {
            Duration stay = Duration.between(ticket.entryAt(), ticket.exitAt());
            model.addAttribute("stay", StayFormat.format(stay));
            model.addAttribute("billedHours", Math.max(1, (stay.toMinutes() + 59) / 60));
        }
        return "tickets/detail";
    }

    // El comprobante en PDF todavia no se genera: se muestra la pagina de "funcion no disponible"
    @GetMapping("/{id}/receipt.pdf")
    public ModelAndView receipt(@PathVariable UUID id) {
        return new ModelAndView("error/unavailable", HttpStatus.NOT_IMPLEMENTED);
    }
}
