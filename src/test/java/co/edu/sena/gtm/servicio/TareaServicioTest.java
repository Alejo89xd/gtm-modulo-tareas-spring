package co.edu.sena.gtm.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.sena.gtm.dto.TareaFormulario;
import co.edu.sena.gtm.excepcion.ReglaNegocioException;
import co.edu.sena.gtm.excepcion.TareaNoEncontradaException;
import co.edu.sena.gtm.modelo.EstadoTarea;
import co.edu.sena.gtm.modelo.HistorialEstado;
import co.edu.sena.gtm.modelo.PrioridadTarea;
import co.edu.sena.gtm.modelo.Proyecto;
import co.edu.sena.gtm.modelo.Tarea;
import co.edu.sena.gtm.modelo.Usuario;
import co.edu.sena.gtm.repositorio.HistorialRepositorio;
import co.edu.sena.gtm.repositorio.ProyectoRepositorio;
import co.edu.sena.gtm.repositorio.TareaRepositorio;
import co.edu.sena.gtm.repositorio.UsuarioRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Pruebas unitarias de {@link TareaServicio}. No arrancan Spring ni usan MySQL:
 * los repositorios se reemplazan por versiones en memoria (fakes) definidas al
 * final de esta clase, así las pruebas son rápidas y no dependen de nada externo.
 */
class TareaServicioTest {

    private TareaRepositorioFalso tareas;
    private HistorialRepositorioFalso historiales;
    private TareaServicio servicio;

    @BeforeEach
    void preparar() {
        tareas = new TareaRepositorioFalso();
        historiales = new HistorialRepositorioFalso();
        ProyectoRepositorio proyectos = new ProyectoRepositorioFalso(
                new Proyecto(1, "Sistema GTM"), new Proyecto(2, "Portal de Ventas"));
        UsuarioRepositorio usuarios = new UsuarioRepositorioFalso(
                new Usuario(2, "Carlos", "Ramirez", true), new Usuario(4, "Juan", "Martinez", true));
        servicio = new TareaServicio(tareas, proyectos, usuarios, historiales);
    }

    // ----------------------------------------------------------------- crear

    @Test
    void crearAsignaCreadorYEstadoPendiente() {
        Tarea creada = servicio.crear(formularioValido());

        assertNotNull(creada.getId());
        assertEquals(EstadoTarea.PENDIENTE, creada.getEstado());
        assertEquals("Carlos Ramirez", creada.getCreador().getNombreCompleto());
        assertEquals("Sistema GTM", creada.getProyecto().getNombre());
        assertEquals(PrioridadTarea.ALTA, creada.getPrioridad());
    }

    @Test
    void crearLimpiaEspaciosYDescripcionVacia() {
        TareaFormulario formulario = formularioValido();
        formulario.setTitulo("   Diseñar base de datos   ");
        formulario.setDescripcion("    ");

        Tarea creada = servicio.crear(formulario);

        assertEquals("Diseñar base de datos", creada.getTitulo());
        assertNull(creada.getDescripcion());
    }

    @Test
    void crearRechazaFechaLimitePasada() {
        TareaFormulario formulario = formularioValido();
        formulario.setFechaLimite(LocalDate.now().minusDays(1));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(formulario));

        assertTrue(error.getMessage().contains("fecha límite"));
        assertTrue(tareas.datos.isEmpty());
    }

    @Test
    void crearExigeCreador() {
        TareaFormulario formulario = formularioValido();
        formulario.setCreadorId(null);

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(formulario));
    }

    @Test
    void crearRechazaProyectoInexistente() {
        TareaFormulario formulario = formularioValido();
        formulario.setProyectoId(99);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(formulario));

        assertTrue(error.getMessage().contains("proyecto"));
    }

    // ------------------------------------------------------------ actualizar

    @Test
    void actualizarCambiaDatosPeroNoEstadoNiCreador() {
        Tarea creada = servicio.crear(formularioValido());
        servicio.cambiarEstado(creada.getId(), EstadoTarea.EN_PROGRESO, 2, null);

        TareaFormulario cambios = formularioValido();
        cambios.setTitulo("Título nuevo");
        cambios.setPrioridad(PrioridadTarea.BAJA);
        cambios.setProyectoId(2);
        cambios.setCreadorId(4); // debe ignorarse: el creador no se modifica

        Tarea actualizada = servicio.actualizar(creada.getId(), cambios);

        assertEquals("Título nuevo", actualizada.getTitulo());
        assertEquals(PrioridadTarea.BAJA, actualizada.getPrioridad());
        assertEquals("Portal de Ventas", actualizada.getProyecto().getNombre());
        assertEquals(EstadoTarea.EN_PROGRESO, actualizada.getEstado());
        assertEquals("Carlos Ramirez", actualizada.getCreador().getNombreCompleto());
    }

    @Test
    void actualizarTareaInexistenteLanzaExcepcion() {
        assertThrows(TareaNoEncontradaException.class,
                () -> servicio.actualizar(99, formularioValido()));
    }

    // ---------------------------------------------------------------- buscar

    @Test
    void buscarSinFiltrosDevuelveTodas() {
        crearTresTareas();

        assertEquals(3, servicio.buscar(null, null).size());
        assertEquals(3, servicio.buscar("   ", null).size());
    }

    @Test
    void buscarPorTextoNoDistingueMayusculas() {
        crearTresTareas();

        List<Tarea> resultado = servicio.buscar("jwt", null);

        assertEquals(1, resultado.size());
        assertEquals("Implementar autenticación JWT", resultado.get(0).getTitulo());
    }

    @Test
    void buscarPorEstado() {
        crearTresTareas();
        servicio.cambiarEstado(1, EstadoTarea.COMPLETADA, 2, null);

        assertEquals(1, servicio.buscar(null, EstadoTarea.COMPLETADA).size());
        assertEquals(2, servicio.buscar(null, EstadoTarea.PENDIENTE).size());
    }

    @Test
    void buscarCombinandoTextoYEstado() {
        crearTresTareas();
        servicio.cambiarEstado(1, EstadoTarea.EN_PROGRESO, 2, null);

        assertEquals(1, servicio.buscar("autenticación", EstadoTarea.EN_PROGRESO).size());
        assertTrue(servicio.buscar("autenticación", EstadoTarea.COMPLETADA).isEmpty());
    }

    // ---------------------------------------------------------- cambiarEstado

    @Test
    void cambiarEstadoRegistraHistorialConEstadoAnterior() {
        Tarea creada = servicio.crear(formularioValido());

        servicio.cambiarEstado(creada.getId(), EstadoTarea.EN_PROGRESO, 2, "  Inicio del desarrollo  ");

        assertEquals(EstadoTarea.EN_PROGRESO, servicio.obtener(creada.getId()).getEstado());
        assertEquals(1, historiales.registros.size());
        HistorialEstado registro = historiales.registros.get(0);
        assertEquals(EstadoTarea.PENDIENTE, registro.getEstadoAnterior());
        assertEquals(EstadoTarea.EN_PROGRESO, registro.getEstadoNuevo());
        assertEquals("Carlos Ramirez", registro.getCambiadoPor().getNombreCompleto());
        assertEquals("Inicio del desarrollo", registro.getObservacion());
    }

    @Test
    void cambiarEstadoGuardaNullSiLaObservacionVieneVacia() {
        Tarea creada = servicio.crear(formularioValido());

        servicio.cambiarEstado(creada.getId(), EstadoTarea.EN_REVISION, 2, "   ");

        assertNull(historiales.registros.get(0).getObservacion());
    }

    @Test
    void cambiarAlMismoEstadoLanzaExcepcionYNoRegistraHistorial() {
        Tarea creada = servicio.crear(formularioValido());

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado(creada.getId(), EstadoTarea.PENDIENTE, 2, null));

        assertTrue(error.getMessage().contains("ya se encuentra"));
        assertTrue(historiales.registros.isEmpty());
    }

    @Test
    void cambiarEstadoExigeUsuarioValido() {
        Tarea creada = servicio.crear(formularioValido());

        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado(creada.getId(), EstadoTarea.COMPLETADA, null, null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado(creada.getId(), EstadoTarea.COMPLETADA, 99, null));
        assertEquals(EstadoTarea.PENDIENTE, servicio.obtener(creada.getId()).getEstado());
        assertTrue(historiales.registros.isEmpty());
    }

    @Test
    void cambiarEstadoDeTareaInexistenteLanzaExcepcion() {
        assertThrows(TareaNoEncontradaException.class,
                () -> servicio.cambiarEstado(99, EstadoTarea.COMPLETADA, 2, null));
    }

    // -------------------------------------------------------------- eliminar

    @Test
    void eliminarQuitaLaTarea() {
        Tarea creada = servicio.crear(formularioValido());

        servicio.eliminar(creada.getId());

        assertTrue(tareas.datos.isEmpty());
        assertThrows(TareaNoEncontradaException.class, () -> servicio.obtener(creada.getId()));
    }

    @Test
    void eliminarInexistenteLanzaExcepcion() {
        assertThrows(TareaNoEncontradaException.class, () -> servicio.eliminar(99));
    }

    // ----------------------------------------------------------- entidad Tarea

    @Test
    void unaTareaPendienteConFechaPasadaEstaVencida() {
        Tarea tarea = new Tarea();
        tarea.setFechaLimite(LocalDate.now().minusDays(3));
        assertTrue(tarea.isVencida());

        tarea.setEstado(EstadoTarea.COMPLETADA);
        assertFalse(tarea.isVencida());

        tarea.setEstado(EstadoTarea.PENDIENTE);
        tarea.setFechaLimite(null);
        assertFalse(tarea.isVencida());
    }

    // --------------------------------------------------------------- auxiliares

    private TareaFormulario formularioValido() {
        TareaFormulario formulario = new TareaFormulario();
        formulario.setTitulo("Implementar autenticación JWT");
        formulario.setProyectoId(1);
        formulario.setCreadorId(2);
        formulario.setPrioridad(PrioridadTarea.ALTA);
        formulario.setFechaLimite(LocalDate.now().plusDays(5));
        return formulario;
    }

    private void crearTresTareas() {
        servicio.crear(formularioValido());
        TareaFormulario segunda = formularioValido();
        segunda.setTitulo("Diseñar modelo de base de datos");
        servicio.crear(segunda);
        TareaFormulario tercera = formularioValido();
        tercera.setTitulo("Configurar Docker Compose");
        servicio.crear(tercera);
    }

    // ------------------------------------------------------ repositorios falsos

    /** Repositorio de tareas en memoria. */
    private static class TareaRepositorioFalso implements TareaRepositorio {
        private final Map<Integer, Tarea> datos = new TreeMap<>();
        private int siguienteId = 1;

        @Override
        public Tarea save(Tarea tarea) {
            if (tarea.getId() == null) {
                tarea.setId(siguienteId++);
            }
            datos.put(tarea.getId(), tarea);
            return tarea;
        }

        @Override
        public Optional<Tarea> findById(Integer id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public boolean existsById(Integer id) {
            return datos.containsKey(id);
        }

        @Override
        public void deleteById(Integer id) {
            datos.remove(id);
        }

        @Override
        public List<Tarea> findAllByOrderByIdAsc() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public List<Tarea> findByTituloContainingIgnoreCaseOrderByIdAsc(String texto) {
            return filtrar(texto, null);
        }

        @Override
        public List<Tarea> findByEstadoOrderByIdAsc(EstadoTarea estado) {
            return filtrar(null, estado);
        }

        @Override
        public List<Tarea> findByTituloContainingIgnoreCaseAndEstadoOrderByIdAsc(String texto,
                EstadoTarea estado) {
            return filtrar(texto, estado);
        }

        private List<Tarea> filtrar(String texto, EstadoTarea estado) {
            List<Tarea> resultado = new ArrayList<>();
            for (Tarea tarea : datos.values()) {
                boolean coincideTexto = texto == null
                        || tarea.getTitulo().toLowerCase().contains(texto.toLowerCase());
                boolean coincideEstado = estado == null || tarea.getEstado() == estado;
                if (coincideTexto && coincideEstado) {
                    resultado.add(tarea);
                }
            }
            return resultado;
        }
    }

    /** Repositorio de proyectos en memoria. */
    private static class ProyectoRepositorioFalso implements ProyectoRepositorio {
        private final List<Proyecto> proyectos = new ArrayList<>();

        ProyectoRepositorioFalso(Proyecto... iniciales) {
            for (Proyecto proyecto : iniciales) {
                proyectos.add(proyecto);
            }
        }

        @Override
        public Optional<Proyecto> findById(Integer id) {
            return proyectos.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Proyecto> findAllByOrderByNombreAsc() {
            return new ArrayList<>(proyectos);
        }
    }

    /** Repositorio de usuarios en memoria. */
    private static class UsuarioRepositorioFalso implements UsuarioRepositorio {
        private final List<Usuario> usuarios = new ArrayList<>();

        UsuarioRepositorioFalso(Usuario... iniciales) {
            for (Usuario usuario : iniciales) {
                usuarios.add(usuario);
            }
        }

        @Override
        public Optional<Usuario> findById(Integer id) {
            return usuarios.stream().filter(u -> u.getId().equals(id)).findFirst();
        }

        @Override
        public List<Usuario> findByActivoTrueOrderByNombreAsc() {
            return new ArrayList<>(usuarios);
        }
    }

    /** Repositorio del historial en memoria. */
    private static class HistorialRepositorioFalso implements HistorialRepositorio {
        private final List<HistorialEstado> registros = new ArrayList<>();

        @Override
        public HistorialEstado save(HistorialEstado historial) {
            registros.add(historial);
            return historial;
        }

        @Override
        public List<HistorialEstado> findByTareaIdOrderByFechaCambioDescIdDesc(Integer tareaId) {
            List<HistorialEstado> resultado = new ArrayList<>();
            for (HistorialEstado registro : registros) {
                if (registro.getTarea().getId().equals(tareaId)) {
                    resultado.add(registro);
                }
            }
            return resultado;
        }
    }
}
