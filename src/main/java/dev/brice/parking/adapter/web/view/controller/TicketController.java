package dev.brice.parking.adapter.web.view.controller;

import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final ParkingService parking;
    private final SlotService slots;
    private final ReportService reports;

    public TicketController(ParkingService parking, SlotService slots, ReportService reports) {
        this.parking = parking;
        this.slots = slots;
        this.reports = reports;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tickets", parking.findAllTickets());
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

    @PostMapping("/{id}/check-out")
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
                .orElseThrow(() -> new IllegalArgumentException("El ticket no existe"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("slot", slots.findSlotById(ticket.slotId()).orElse(null));
        return "tickets/detail";
    }

    @GetMapping("/{id}/receipt.pdf")
    public ResponseEntity<byte[]> receipt(@PathVariable UUID id) {
        byte[] pdf = reports.generateReceipt(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("comprobante.pdf").build().toString())
                .body(pdf);
    }
}
