package co.edu.sena.gtm.servicio;

import co.edu.sena.gtm.dto.TareaFormulario;
import co.edu.sena.gtm.excepcion.ReglaNegocioException;
import co.edu.sena.gtm.excepcion.TareaNoEncontradaException;
import co.edu.sena.gtm.modelo.EstadoTarea;
import co.edu.sena.gtm.modelo.HistorialEstado;
import co.edu.sena.gtm.modelo.Proyecto;
import co.edu.sena.gtm.modelo.Tarea;
import co.edu.sena.gtm.modelo.Usuario;
import co.edu.sena.gtm.repositorio.HistorialRepositorio;
import co.edu.sena.gtm.repositorio.ProyectoRepositorio;
import co.edu.sena.gtm.repositorio.TareaRepositorio;
import co.edu.sena.gtm.repositorio.UsuarioRepositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio del módulo Tareas. Spring inyecta los repositorios por el
 * constructor (inyección de dependencias). Los métodos que modifican datos son
 * transaccionales: si algo falla a mitad de camino, se deshace todo.
 */
@Service
public class TareaServicio {

    private final TareaRepositorio tareaRepositorio;
    private final ProyectoRepositorio proyectoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final HistorialRepositorio historialRepositorio;

    public TareaServicio(TareaRepositorio tareaRepositorio, ProyectoRepositorio proyectoRepositorio,
            UsuarioRepositorio usuarioRepositorio, HistorialRepositorio historialRepositorio) {
        this.tareaRepositorio = tareaRepositorio;
        this.proyectoRepositorio = proyectoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.historialRepositorio = historialRepositorio;
    }

    /**
     * Busca tareas combinando dos filtros opcionales.
     *
     * @param texto  fragmento del título, sin distinguir mayúsculas (null o vacío = sin filtro)
     * @param estado estado exacto (null = sin filtro)
     */
    @Transactional(readOnly = true)
    public List<Tarea> buscar(String texto, EstadoTarea estado) {
        String filtro = texto == null ? "" : texto.trim();
        boolean hayTexto = !filtro.isEmpty();
        if (hayTexto && estado != null) {
            return tareaRepositorio.findByTituloContainingIgnoreCaseAndEstadoOrderByIdAsc(filtro, estado);
        }
        if (hayTexto) {
            return tareaRepositorio.findByTituloContainingIgnoreCaseOrderByIdAsc(filtro);
        }
        if (estado != null) {
            return tareaRepositorio.findByEstadoOrderByIdAsc(estado);
        }
        return tareaRepositorio.findAllByOrderByIdAsc();
    }

    /**
     * Devuelve una tarea o lanza {@link TareaNoEncontradaException} si no existe.
     */
    @Transactional(readOnly = true)
    public Tarea obtener(Integer id) {
        return tareaRepositorio.findById(id).orElseThrow(() -> new TareaNoEncontradaException(id));
    }

    /**
     * Crea una tarea nueva. Siempre nace en estado PENDIENTE.
     *
     * @throws ReglaNegocioException si la fecha ya pasó o el proyecto/usuario no existen
     */
    @Transactional
    public Tarea crear(TareaFormulario formulario) {
        validarFechaLimite(formulario.getFechaLimite());
        Tarea tarea = new Tarea();
        tarea.setCreador(buscarUsuario(formulario.getCreadorId(), "Seleccione quién crea la tarea."));
        tarea.setEstado(EstadoTarea.PENDIENTE);
        copiarDatos(formulario, tarea);
        return tareaRepositorio.save(tarea);
    }

    /**
     * Actualiza los datos editables de una tarea. No cambia su estado ni su creador.
     */
    @Transactional
    public Tarea actualizar(Integer id, TareaFormulario formulario) {
        Tarea tarea = obtener(id);
        copiarDatos(formulario, tarea);
        return tareaRepositorio.save(tarea);
    }

    /**
     * Cambia el estado de una tarea y registra el cambio en el historial. Ambas
     * operaciones ocurren en la misma transacción.
     *
     * @throws ReglaNegocioException si la tarea ya está en ese estado o falta el usuario
     */
    @Transactional
    public void cambiarEstado(Integer tareaId, EstadoTarea nuevoEstado, Integer usuarioId,
            String observacion) {
        Tarea tarea = obtener(tareaId);
        if (tarea.getEstado() == nuevoEstado) {
            throw new ReglaNegocioException(
                    "La tarea ya se encuentra en el estado '" + nuevoEstado.getEtiqueta() + "'.");
        }
        Usuario usuario = buscarUsuario(usuarioId, "Seleccione quién realiza el cambio.");
        String observacionLimpia = observacion == null || observacion.trim().isEmpty()
                ? null : observacion.trim();

        historialRepositorio.save(new HistorialEstado(
                tarea, usuario, tarea.getEstado(), nuevoEstado, observacionLimpia));
        tarea.setEstado(nuevoEstado);
        tareaRepositorio.save(tarea);
    }

    /**
     * Elimina una tarea. Sus comentarios, asignaciones e historial se eliminan
     * en cascada por las llaves foráneas del modelo de datos.
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!tareaRepositorio.existsById(id)) {
            throw new TareaNoEncontradaException(id);
        }
        tareaRepositorio.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<HistorialEstado> historial(Integer tareaId) {
        return historialRepositorio.findByTareaIdOrderByFechaCambioDescIdDesc(tareaId);
    }

    @Transactional(readOnly = true)
    public List<Proyecto> listarProyectos() {
        return proyectoRepositorio.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    // ---------------------------------------------------------------- privados

    /** Pasa los datos del formulario a la entidad (sin tocar estado ni creador). */
    private void copiarDatos(TareaFormulario formulario, Tarea tarea) {
        Proyecto proyecto = proyectoRepositorio.findById(formulario.getProyectoId())
                .orElseThrow(() -> new ReglaNegocioException("El proyecto seleccionado no existe."));
        tarea.setTitulo(formulario.getTitulo().trim());
        String descripcion = formulario.getDescripcion();
        tarea.setDescripcion(descripcion == null || descripcion.trim().isEmpty() ? null : descripcion.trim());
        tarea.setProyecto(proyecto);
        tarea.setPrioridad(formulario.getPrioridad());
        tarea.setFechaLimite(formulario.getFechaLimite());
    }

    private Usuario buscarUsuario(Integer usuarioId, String mensajeSiFalta) {
        if (usuarioId == null) {
            throw new ReglaNegocioException(mensajeSiFalta);
        }
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("El usuario seleccionado no existe."));
    }

    private void validarFechaLimite(LocalDate fechaLimite) {
        if (fechaLimite != null && fechaLimite.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha límite no puede ser anterior a hoy.");
        }
    }
}
