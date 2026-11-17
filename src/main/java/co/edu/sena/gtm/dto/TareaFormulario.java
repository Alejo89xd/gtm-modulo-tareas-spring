package co.edu.sena.gtm.dto;

import co.edu.sena.gtm.modelo.PrioridadTarea;
import co.edu.sena.gtm.modelo.Tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Datos que viajan entre el formulario HTML y el controlador. Las anotaciones
 * de Bean Validation (@NotBlank, @Size, @NotNull) las revisa Spring antes de
 * ejecutar el método del controlador; los errores llegan en el BindingResult.
 */
public class TareaFormulario {

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres.")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres.")
    private String descripcion;

    @NotNull(message = "Seleccione un proyecto.")
    private Integer proyectoId;

    /** Solo se usa al crear; al editar la tarea conserva su creador original. */
    private Integer creadorId;

    @NotNull(message = "Seleccione una prioridad.")
    private PrioridadTarea prioridad = PrioridadTarea.MEDIA;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaLimite;

    /** Crea un formulario con los datos actuales de una tarea (para editarla). */
    public static TareaFormulario desde(Tarea tarea) {
        TareaFormulario formulario = new TareaFormulario();
        formulario.titulo = tarea.getTitulo();
        formulario.descripcion = tarea.getDescripcion();
        formulario.proyectoId = tarea.getProyecto().getId();
        formulario.creadorId = tarea.getCreador().getId();
        formulario.prioridad = tarea.getPrioridad();
        formulario.fechaLimite = tarea.getFechaLimite();
        return formulario;
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

    public Integer getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Integer proyectoId) {
        this.proyectoId = proyectoId;
    }

    public Integer getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(Integer creadorId) {
        this.creadorId = creadorId;
    }

    public PrioridadTarea getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadTarea prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
