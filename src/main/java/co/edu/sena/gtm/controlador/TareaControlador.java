package co.edu.sena.gtm.controlador;

import co.edu.sena.gtm.dto.TareaFormulario;
import co.edu.sena.gtm.excepcion.ReglaNegocioException;
import co.edu.sena.gtm.modelo.EstadoTarea;
import co.edu.sena.gtm.modelo.PrioridadTarea;
import co.edu.sena.gtm.modelo.Tarea;
import co.edu.sena.gtm.servicio.TareaServicio;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC del módulo Tareas. Cada método responde a una URL y devuelve
 * el nombre de la plantilla Thymeleaf a mostrar o una redirección.
 *
 * <p>Las consultas usan GET. Las operaciones que modifican datos usan POST y
 * terminan en una redirección (patrón Post/Redirect/Get) para que recargar la
 * página no repita la operación.</p>
 */
@Controller
@RequestMapping("/tareas")
public class TareaControlador {

    private static final String VISTA_FORMULARIO = "tareas/formulario";
    private static final String CLAVE_EXITO = "mensajeExito";
    private static final String CLAVE_ERROR = "mensajeError";

    private final TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    /** GET /tareas : lista y filtra (parámetros opcionales q y estado). */
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoTarea estado, Model modelo) {
        modelo.addAttribute("tareas", tareaServicio.buscar(q, estado));
        modelo.addAttribute("estados", EstadoTarea.values());
        modelo.addAttribute("textoBusqueda", q == null ? "" : q);
        modelo.addAttribute("estadoFiltro", estado);
        return "tareas/lista";
    }

    /** GET /tareas/nueva : formulario vacío. */
    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("tarea", new TareaFormulario());
        prepararFormulario(modelo, null, null);
        return VISTA_FORMULARIO;
    }

    /** POST /tareas : valida y crea la tarea. */
    @PostMapping
    public String crear(@Valid @ModelAttribute("tarea") TareaFormulario formulario,
            BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (formulario.getCreadorId() == null) {
            resultado.rejectValue("creadorId", "obligatorio", "Seleccione quién crea la tarea.");
        }
        if (!resultado.hasErrors()) {
            try {
                Tarea creada = tareaServicio.crear(formulario);
                mensajes.addFlashAttribute(CLAVE_EXITO, "Tarea " + creada.getId() + " creada.");
                return "redirect:/tareas/" + creada.getId();
            } catch (ReglaNegocioException excepcion) {
                resultado.reject("reglaNegocio", excepcion.getMessage());
            }
        }
        prepararFormulario(modelo, null, null);
        return VISTA_FORMULARIO;
    }

    /** GET /tareas/{id}/editar : formulario con los datos de la tarea. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo) {
        Tarea tarea = tareaServicio.obtener(id);
        modelo.addAttribute("tarea", TareaFormulario.desde(tarea));
        prepararFormulario(modelo, id, tarea.getCreador().getNombreCompleto());
        return VISTA_FORMULARIO;
    }

    /** POST /tareas/{id} : valida y actualiza la tarea. */
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("tarea") TareaFormulario formulario,
            BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                tareaServicio.actualizar(id, formulario);
                mensajes.addFlashAttribute(CLAVE_EXITO, "Tarea " + id + " actualizada.");
                return "redirect:/tareas/" + id;
            } catch (ReglaNegocioException excepcion) {
                resultado.reject("reglaNegocio", excepcion.getMessage());
            }
        }
        prepararFormulario(modelo, id, tareaServicio.obtener(id).getCreador().getNombreCompleto());
        return VISTA_FORMULARIO;
    }

    /** GET /tareas/{id} : detalle con historial y formulario de cambio de estado. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model modelo) {
        modelo.addAttribute("tarea", tareaServicio.obtener(id));
        modelo.addAttribute("historial", tareaServicio.historial(id));
        modelo.addAttribute("estados", EstadoTarea.values());
        modelo.addAttribute("usuarios", tareaServicio.listarUsuarios());
        return "tareas/detalle";
    }

    /** POST /tareas/{id}/estado : cambia el estado y registra el historial. */
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id, @RequestParam EstadoTarea estado,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String observacion, RedirectAttributes mensajes) {
        try {
            tareaServicio.cambiarEstado(id, estado, usuarioId, observacion);
            mensajes.addFlashAttribute(CLAVE_EXITO,
                    "Estado actualizado a '" + estado.getEtiqueta() + "'.");
        } catch (ReglaNegocioException excepcion) {
            mensajes.addFlashAttribute(CLAVE_ERROR, excepcion.getMessage());
        }
        return "redirect:/tareas/" + id;
    }

    /** POST /tareas/{id}/eliminar : elimina la tarea. */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes mensajes) {
        tareaServicio.eliminar(id);
        mensajes.addFlashAttribute(CLAVE_EXITO, "Tarea " + id + " eliminada.");
        return "redirect:/tareas";
    }

    /**
     * Agrega al modelo las listas que necesita el formulario.
     *
     * @param idTarea       id de la tarea que se edita, o null si es nueva
     * @param nombreCreador nombre del creador al editar, o null si es nueva
     */
    private void prepararFormulario(Model modelo, Integer idTarea, String nombreCreador) {
        modelo.addAttribute("esEdicion", idTarea != null);
        modelo.addAttribute("idTarea", idTarea);
        modelo.addAttribute("nombreCreador", nombreCreador);
        modelo.addAttribute("proyectos", tareaServicio.listarProyectos());
        modelo.addAttribute("usuarios", tareaServicio.listarUsuarios());
        modelo.addAttribute("prioridades", PrioridadTarea.values());
    }
}
