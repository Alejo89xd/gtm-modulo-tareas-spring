package co.edu.sena.gtm.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Proyecto al que pertenece una tarea (tabla proyectos). En este módulo solo
 * se lee, para llenar la lista desplegable y mostrar el nombre.
 */
@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    /** Constructor sin argumentos exigido por JPA. */
    protected Proyecto() {
    }

    public Proyecto(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
