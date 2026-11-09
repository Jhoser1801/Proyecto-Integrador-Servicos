package dev.brice.parking.adapter.web.view.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.port.in.TariffUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/tariffs")
public class TariffController {
    private final TariffUseCase tariffs;

    public TariffController(TariffUseCase tariffs) {
        this.tariffs = tariffs;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tariffs", tariffs.findAllTariffs());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("form", new TariffDTO(VehicleType.CAR, BigDecimal.ZERO));
        return "tariffs/list";
    }

    @PostMapping
    public String save(@ModelAttribute("form") TariffDTO form, RedirectAttributes redirect) {
        try {
            tariffs.setTariff(form);
            redirect.addFlashAttribute("success", "Tarifa guardada");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tariffs";
    }
}
