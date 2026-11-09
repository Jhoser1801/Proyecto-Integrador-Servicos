package dev.brice.parking.adapter.web.view.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ViewExceptionHandler {

    // Reporte PDF todavia sin implementar (o no disponible): pagina amigable, no un stack trace
    @ExceptionHandler(UnsupportedOperationException.class)
    public ModelAndView handleUnavailable(UnsupportedOperationException e) {
        ModelAndView view = new ModelAndView("error/unavailable", HttpStatus.NOT_IMPLEMENTED);
        view.addObject("message", e.getMessage());
        return view;
    }

    // Reglas del core que se rompen en peticiones GET (ids invalidos, tickets inexistentes...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleBadRequest(IllegalArgumentException e) {
        ModelAndView view = new ModelAndView("error/400", HttpStatus.BAD_REQUEST);
        view.addObject("message", e.getMessage());
        return view;
    }
}
