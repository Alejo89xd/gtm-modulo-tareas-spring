package co.edu.sena.gtm.modelo;

/**
 * Prioridades posibles de una tarea (columna ENUM tareas.prioridad).
 */
public enum PrioridadTarea {
    ALTA("alta", "Alta"),
    MEDIA("media", "Media"),
    BAJA("baja", "Baja");

    private final String valorBd;
    private final String etiqueta;

    PrioridadTarea(String valorBd, String etiqueta) {
        this.valorBd = valorBd;
        this.etiqueta = etiqueta;
    }

    /** Texto tal como se guarda en MySQL. */
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
     * @param valor texto del ENUM, por ejemplo "alta"
     * @return la constante correspondiente
     * @throws IllegalArgumentException si el texto no corresponde a ninguna prioridad
     */
    public static PrioridadTarea desdeValorBd(String valor) {
        for (PrioridadTarea prioridad : values()) {
            if (prioridad.valorBd.equals(valor)) {
                return prioridad;
            }
        }
        throw new IllegalArgumentException("Prioridad desconocida: " + valor);
    }
}
