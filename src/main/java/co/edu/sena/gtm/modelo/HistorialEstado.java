package co.edu.sena.gtm.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro de un cambio de estado de una tarea (tabla historial_estados).
 */
@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cambiado_por", nullable = false)
    private Usuario cambiadoPor;

    /** Estado previo; es null cuando se trata del primer registro de la tarea. */
    @Convert(converter = EstadoTareaConvertidor.class)
    @Column(name = "estado_anterior")
    private EstadoTarea estadoAnterior;

    @Convert(converter = EstadoTareaConvertidor.class)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoTarea estadoNuevo;

    @Column(name = "observacion")
    private String observacion;

    /** La asigna MySQL (DEFAULT CURRENT_TIMESTAMP); Hibernate solo la lee. */
    @Column(name = "fecha_cambio", insertable = false, updatable = false)
    private LocalDateTime fechaCambio;

    /** Constructor sin argumentos exigido por JPA. */
    protected HistorialEstado() {
    }

    public HistorialEstado(Tarea tarea, Usuario cambiadoPor, EstadoTarea estadoAnterior,
            EstadoTarea estadoNuevo, String observacion) {
        this.tarea = tarea;
        this.cambiadoPor = cambiadoPor;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
    }

    public Integer getId() {
        return id;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public Usuario getCambiadoPor() {
        return cambiadoPor;
    }

    public EstadoTarea getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoTarea getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getObservacion() {
        return observacion;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }
}
