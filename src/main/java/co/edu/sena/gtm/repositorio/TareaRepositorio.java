package co.edu.sena.gtm.repositorio;

import co.edu.sena.gtm.modelo.EstadoTarea;
import co.edu.sena.gtm.modelo.Tarea;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de las tareas. Spring Data genera la implementación en tiempo
 * de ejecución a partir de los nombres de los métodos ("consultas derivadas").
 * Extiende {@link Repository} en lugar de JpaRepository para exponer solo las
 * operaciones que el módulo realmente usa.
 */
public interface TareaRepositorio extends Repository<Tarea, Integer> {

    Tarea save(Tarea tarea);

    Optional<Tarea> findById(Integer id);

    boolean existsById(Integer id);

    void deleteById(Integer id);

    List<Tarea> findAllByOrderByIdAsc();

    List<Tarea> findByTituloContainingIgnoreCaseOrderByIdAsc(String texto);

    List<Tarea> findByEstadoOrderByIdAsc(EstadoTarea estado);

    List<Tarea> findByTituloContainingIgnoreCaseAndEstadoOrderByIdAsc(String texto, EstadoTarea estado);
}
