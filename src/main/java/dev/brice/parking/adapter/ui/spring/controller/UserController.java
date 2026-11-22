package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.dto.UserUpdateDTO;
import dev.brice.parking.core.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

// Solo el administrador (ver SecurityConfig): crear, actualizar y eliminar operadores y administradores
@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        model.addAttribute("users", users.findAllUsers());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("form", new UserDTO("", "", UserRole.OPERATOR));
        model.addAttribute("currentUsername", principal.getName());
        return "users/list";
    }

    @PostMapping
    public String create(@ModelAttribute("form") UserDTO form, RedirectAttributes redirect) {
        try {
            users.createUser(form);
            redirect.addFlashAttribute("success", "Usuario creado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    // PUT: cambia el rol y, si se escribe una contrasena nueva, tambien la contrasena
    @PutMapping("/{id}")
    public String update(@PathVariable UUID id, @ModelAttribute UserUpdateDTO form, RedirectAttributes redirect) {
        try {
            users.updateUser(id, form);
            redirect.addFlashAttribute("success", "Usuario actualizado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id, Principal principal, RedirectAttributes redirect) {
        try {
            users.deleteUser(id, principal.getName());
            redirect.addFlashAttribute("success", "Usuario eliminado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }
}
