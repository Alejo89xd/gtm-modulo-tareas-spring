package co.edu.sena.gtm.excepcion;

/**
 * Se lanza cuando se pide una tarea cuyo id no existe. El manejador global
 * de errores la convierte en una página 404.
 */
public class TareaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TareaNoEncontradaException(Integer id) {
        super("No existe una tarea con id " + id + ".");
    }
}
