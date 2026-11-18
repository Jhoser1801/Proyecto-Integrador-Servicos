package dev.brice.parking.adapter.web.view.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @GetMapping
    public String form(Model model) {
        model.addAttribute("from", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("to", LocalDate.now());
        return "reports/form";
    }

    // El PDF todavia no se genera: se muestra la pagina de "funcion no disponible"
    @GetMapping("/revenue.pdf")
    public ModelAndView revenue() {
        return new ModelAndView("error/unavailable", HttpStatus.NOT_IMPLEMENTED);
    }
}
