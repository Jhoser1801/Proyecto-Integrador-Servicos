package dev.brice.parking.adapter.ui.spring.config;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/error", "/error/**", "/cupos").permitAll()
                        // consola web de H2: solo el administrador
                        .requestMatchers("/h2/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/slots/**", "/tariffs/**", "/reports/**", "/users/**").hasRole(UserRole.ADMIN.name())
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }

    // BCrypt de Spring Security: valida el login
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserService users) {
        return username -> users.findUserByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.username())
                        .password(user.passwordHash())
                        .roles(user.role().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
