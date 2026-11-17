package co.edu.sena.gtm.excepcion;

/**
 * Se lanza cuando una operación incumple una regla de negocio (por ejemplo,
 * una fecha límite pasada). El mensaje está pensado para mostrarse al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
