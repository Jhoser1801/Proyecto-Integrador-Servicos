package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    // Por defecto muestra los ultimos 7 dias; el rango se cambia con ?from=yyyy-MM-dd&to=yyyy-MM-dd
    @GetMapping
    public String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(6);
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        try {
            model.addAttribute("summary", reports.buildRevenueSummary(start, end));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reports/form";
    }

    // El PDF todavia no se genera: se muestra la pagina de "funcion no disponible"
    @GetMapping("/revenue.pdf")
    public ModelAndView revenue() {
        return new ModelAndView("error/unavailable", HttpStatus.NOT_IMPLEMENTED);
    }
}
