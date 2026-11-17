package co.edu.sena.gtm.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario del sistema (tabla usuarios). En este módulo solo se lee: es el
 * creador de una tarea o quien registra un cambio de estado.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    /** Constructor sin argumentos exigido por JPA. */
    protected Usuario() {
    }

    public Usuario(Integer id, String nombre, String apellido, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public boolean isActivo() {
        return activo;
    }

    /** Nombre y apellido juntos, para mostrar en pantalla. */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
