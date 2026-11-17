package co.edu.sena.gtm.controlador;

import co.edu.sena.gtm.excepcion.TareaNoEncontradaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores: convierte las excepciones en una página
 * amigable (plantilla error.html) con el código HTTP que corresponde.
 */
@ControllerAdvice
public class ManejadorErrores {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final String VISTA_ERROR = "error";

    /** La tarea pedida no existe: 404. */
    @ExceptionHandler(TareaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String tareaNoEncontrada(TareaNoEncontradaException excepcion, Model modelo) {
        modelo.addAttribute("titulo", "No encontrado");
        modelo.addAttribute("mensaje", excepcion.getMessage());
        return VISTA_ERROR;
    }

    /** Un parámetro de la URL tiene un valor inválido (por ejemplo un estado inventado): 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametroInvalido(MethodArgumentTypeMismatchException excepcion, Model modelo) {
        modelo.addAttribute("titulo", "Petición no válida");
        modelo.addAttribute("mensaje", "El valor del parámetro '" + excepcion.getName() + "' no es válido.");
        return VISTA_ERROR;
    }

    /** Falla la base de datos (apagada, clave incorrecta, error SQL): 500. */
    @ExceptionHandler({DataAccessException.class, CannotCreateTransactionException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorDeBaseDeDatos(Exception excepcion, Model modelo) {
        REGISTRO.error("Error de acceso a la base de datos", excepcion);
        modelo.addAttribute("titulo", "Error de base de datos");
        modelo.addAttribute("mensaje", "No se pudo completar la operación. Revise que MySQL esté "
                + "encendido y que la clave en application-local.properties sea correcta.");
        return VISTA_ERROR;
    }
}
