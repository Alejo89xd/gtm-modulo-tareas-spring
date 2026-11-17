package co.edu.sena.gtm.repositorio;

import co.edu.sena.gtm.modelo.Usuario;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Consulta de usuarios (solo lectura).
 */
public interface UsuarioRepositorio extends Repository<Usuario, Integer> {

    Optional<Usuario> findById(Integer id);

    List<Usuario> findByActivoTrueOrderByNombreAsc();
}
