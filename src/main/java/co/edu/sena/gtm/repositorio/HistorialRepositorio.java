package co.edu.sena.gtm.repositorio;

import co.edu.sena.gtm.modelo.HistorialEstado;

import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Acceso al historial de cambios de estado.
 */
public interface HistorialRepositorio extends Repository<HistorialEstado, Integer> {

    HistorialEstado save(HistorialEstado historial);

    /** El nombre "TareaId" se interpreta como la propiedad tarea.id. */
    List<HistorialEstado> findByTareaIdOrderByFechaCambioDescIdDesc(Integer tareaId);
}
