package co.edu.sena.gtm.repositorio;

import co.edu.sena.gtm.modelo.Proyecto;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Consulta de proyectos (solo lectura).
 */
public interface ProyectoRepositorio extends Repository<Proyecto, Integer> {

    Optional<Proyecto> findById(Integer id);

    List<Proyecto> findAllByOrderByNombreAsc();
}
