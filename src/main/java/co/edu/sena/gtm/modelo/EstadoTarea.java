package co.edu.sena.gtm.modelo;

/**
 * Estados posibles de una tarea. Cada constante guarda el valor exacto que se
 * almacena en la columna ENUM tareas.estado y la etiqueta que ve el usuario.
 */
public enum EstadoTarea {
    PENDIENTE("pendiente", "Pendiente"),
    EN_PROGRESO("en_progreso", "En progreso"),
    EN_REVISION("en_revision", "En revisión"),
    COMPLETADA("completada", "Completada");

    private final String valorBd;
    private final String etiqueta;

    EstadoTarea(String valorBd, String etiqueta) {
        this.valorBd = valorBd;
        this.etiqueta = etiqueta;
    }

    /** Texto tal como se guarda en MySQL (minúsculas con guion bajo). */
    public String getValorBd() {
        return valorBd;
    }

    /** Texto legible que se muestra en las pantallas. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Convierte el texto guardado en la base de datos a su constante.
     *
     * @param valor texto del ENUM, por ejemplo "en_progreso"
     * @return la constante correspondiente
     * @throws IllegalArgumentException si el texto no corresponde a ningún estado
     */
    public static EstadoTarea desdeValorBd(String valor) {
        for (EstadoTarea estado : values()) {
            if (estado.valorBd.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }
}
