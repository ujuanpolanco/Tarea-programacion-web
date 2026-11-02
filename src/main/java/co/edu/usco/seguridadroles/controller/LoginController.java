package co.edu.usco.seguridadroles.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Paginas personalizadas de seguridad.
 * Spring Security se encarga del POST /login y del POST /logout;
 * aqui solo mostramos las vistas.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // @RequestMapping y no @GetMapping: Spring Security hace un "forward" a /403
    // con el MISMO metodo de la peticion rechazada. Si se niega un POST
    // (por ejemplo POST /delete/1), con @GetMapping saldria un error 405
    // en lugar de esta pagina.
    @RequestMapping("/403")
    public String accesoDenegado() {
        return "403";
    }
}
