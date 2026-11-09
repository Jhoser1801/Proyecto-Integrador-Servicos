package dev.brice.parking.adapter.web.view;

import org.springframework.boot.autoconfigure.SpringBootApplication;

// Adaptador de entrada web (Spring MVC + Thymeleaf). Los casos de uso del core llegan como singletons
// registrados desde Application, asi Javalin y Spring comparten exactamente los mismos services.
@SpringBootApplication
public class ViewApplication {
}
