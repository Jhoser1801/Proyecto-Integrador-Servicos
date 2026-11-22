package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.dto.UserUpdateDTO;
import dev.brice.parking.core.entity.User;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

// Solo el administrador: crear, actualizar y eliminar operadores y administradores
@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String crear,
                       @RequestParam(required = false) UUID editar, Model model, Principal principal) {
        List<User> all = users.findAllUsers();
        model.addAttribute("users", all);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("currentUsername", principal.getName());
        model.addAttribute("totalUsers", all.size());
        model.addAttribute("admins", all.stream().filter(u -> u.role() == UserRole.ADMIN).count());
        model.addAttribute("operators", all.stream().filter(u -> u.role() == UserRole.OPERATOR).count());
        model.addAttribute("showCreate", crear != null);
        model.addAttribute("editing", editar == null ? null : users.findUserById(editar).orElse(null));
        return "users/list";
    }

    @PostMapping
    public String create(@ModelAttribute UserDTO form, @RequestParam(defaultValue = "") String confirmPassword,
                         RedirectAttributes redirect) {
        try {
            requireSamePassword(form.password(), confirmPassword);
            users.createUser(form);
            redirect.addFlashAttribute("success", "Usuario creado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    // PUT: cambia el rol y, si se escribe una contraseña nueva, tambien la contraseña
    @PutMapping("/{id}")
    public String update(@PathVariable UUID id, @ModelAttribute UserUpdateDTO form,
                         @RequestParam(defaultValue = "") String confirmPassword, RedirectAttributes redirect) {
        try {
            requireSamePassword(form.password(), confirmPassword);
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

    private void requireSamePassword(String password, String confirmPassword) {
        String first = password == null ? "" : password;
        if (!first.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contrasenas no coinciden");
        }
    }
}
