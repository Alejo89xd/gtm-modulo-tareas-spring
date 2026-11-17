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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tarea del sistema GTM. Se mapea a la tabla tareas creada en el modelo físico
 * de la evidencia GA6 (ddl-auto=none: Hibernate no modifica el esquema).
 */
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Convert(converter = PrioridadTareaConvertidor.class)
    @Column(name = "prioridad", nullable = false)
    private PrioridadTarea prioridad;

    @Convert(converter = EstadoTareaConvertidor.class)
    @Column(name = "estado", nullable = false)
    private EstadoTarea estado;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    /** El creador se define al crear la tarea y no cambia después. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id", nullable = false, updatable = false)
    private Usuario creador;

    /** La asigna MySQL (DEFAULT CURRENT_TIMESTAMP); Hibernate solo la lee. */
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** La actualiza MySQL (ON UPDATE CURRENT_TIMESTAMP); Hibernate solo la lee. */
    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    /** Constructor sin argumentos exigido por JPA y usado por el servicio. */
    public Tarea() {
        this.prioridad = PrioridadTarea.MEDIA;
        this.estado = EstadoTarea.PENDIENTE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public PrioridadTarea getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadTarea prioridad) {
        this.prioridad = prioridad;
    }

    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    /**
     * Indica si la tarea tiene fecha límite pasada y aún no está completada.
     * Las plantillas la usan como {@code tarea.vencida}.
     */
    public boolean isVencida() {
        return fechaLimite != null
                && estado != EstadoTarea.COMPLETADA
                && fechaLimite.isBefore(LocalDate.now());
    }
}
