package co.edu.sena.gtm.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Atiende la raíz del sitio y la envía al listado de tareas.
 */
@Controller
public class InicioControlador {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/tareas";
    }
}
