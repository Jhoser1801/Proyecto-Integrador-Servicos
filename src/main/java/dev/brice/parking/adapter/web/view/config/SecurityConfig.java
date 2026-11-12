package dev.brice.parking.adapter.web.view.config;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.service.UserService;
import dev.brice.parking.core.port.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/error", "/error/**").permitAll()
                        .requestMatchers("/slots/**", "/tariffs/**", "/reports/**").hasRole(UserRole.ADMIN.name())
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }

    // Spring Security valida la contrasena con el mismo PasswordHasher (puerto) que usa el core
    @Bean
    PasswordEncoder passwordEncoder(PasswordHasher hasher) {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return hasher.hash(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return hasher.matches(rawPassword.toString(), encodedPassword);
            }
        };
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
