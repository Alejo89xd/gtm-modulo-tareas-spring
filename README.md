# GTM - Módulo Tareas (Spring Boot)

Módulo web de gestión de tareas del **Sistema de Gestión de Tareas (GTM)** construido con el
framework **Spring Boot**. Permite crear, listar, buscar, editar, cambiar de estado y eliminar
tareas, y consulta el historial de cambios de estado.

- Aprendiz: Alejandro Diaz
- Evidencia: GA7-220501096-AA3-EV01
- Programa: Tecnología en Análisis y Desarrollo de Sistemas de Información - SENA

## Frameworks y tecnologías

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Spring Boot | 3.3.2 | Arranque y configuración automática; Tomcat embebido |
| Spring MVC | (incluido) | Controladores y rutas HTTP |
| Spring Data JPA + Hibernate | (incluido) | Acceso a MySQL sin escribir SQL a mano |
| Thymeleaf | (incluido) | Plantillas HTML dinámicas |
| Bean Validation | (incluido) | Validación declarativa del formulario |
| JUnit 5 | (incluido) | Pruebas unitarias |
| MySQL | 8.0 | Base de datos `gtm_db` (modelo físico de GA6) |
| Java / Maven | 17 / 3.8+ | Lenguaje y construcción |

## Requisitos

JDK 17 o superior, Apache Maven 3.8 o superior, MySQL Server 8.0 y conexión a internet en la
primera ejecución (Maven descarga las dependencias).

## Puesta en marcha

1. **Base de datos.** Ejecute `sql/gtm_db_script.sql` en MySQL Workbench. El script hace
   `DROP DATABASE IF EXISTS gtm_db`, recrea las 9 tablas y carga datos de prueba.
2. **Clave de MySQL.** Edite `src/main/resources/application-local.properties` y escriba su clave en
   `spring.datasource.password`. Ese archivo está en `.gitignore`; la plantilla es
   `application-local.properties.example`.
3. **Ejecutar** (desde la carpeta del proyecto):

```bash
mvn spring-boot:run
```

4. Abra **http://localhost:8080/** en el navegador. Para detener el servidor, `Ctrl + C`.

**Pruebas unitarias:** `mvn test` (19 pruebas del servicio; no necesitan MySQL).

## Rutas

| Método | URL | Qué hace | Vista |
|---|---|---|---|
| GET | `/tareas?q=...&estado=...` | Lista y filtra tareas | `tareas/lista` |
| GET | `/tareas/nueva` | Formulario vacío | `tareas/formulario` |
| POST | `/tareas` | Valida y crea | formulario o redirección |
| GET | `/tareas/{id}` | Detalle e historial | `tareas/detalle` |
| GET | `/tareas/{id}/editar` | Formulario con los datos | `tareas/formulario` |
| POST | `/tareas/{id}` | Valida y actualiza | formulario o redirección |
| POST | `/tareas/{id}/estado` | Cambia estado y registra historial | redirección |
| POST | `/tareas/{id}/eliminar` | Elimina | redirección |

Las consultas usan GET y las operaciones que modifican datos usan POST, seguidas de una
redirección (patrón Post/Redirect/Get).

## Qué aporta el framework (frente a Servlets + JDBC)

| Característica de Spring | Dónde se ve |
|---|---|
| Inyección de dependencias | `TareaServicio` y `TareaControlador` reciben sus dependencias por el constructor |
| Spring MVC | `@Controller`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam` en `TareaControlador` |
| Spring Data JPA | Interfaces `*Repositorio`: Spring genera la implementación a partir del nombre del método (`findByTituloContainingIgnoreCaseAndEstadoOrderByIdAsc`) |
| Mapeo objeto-relacional | Entidades `Tarea`, `Proyecto`, `Usuario`, `HistorialEstado` con `@Entity`, `@ManyToOne` |
| Convertidores JPA | `EstadoTareaConvertidor` traduce `EN_PROGRESO` (Java) a `en_progreso` (ENUM de MySQL) |
| Transacciones declarativas | `@Transactional` en `TareaServicio`; `cambiarEstado` guarda el estado y el historial juntos o no guarda nada |
| Validación declarativa | `@NotBlank`, `@Size`, `@NotNull` en `TareaFormulario` + `@Valid` en el controlador |
| Plantillas | Thymeleaf: `th:each`, `th:if`, `th:field`, `th:errors`, fragmentos reutilizables |
| Manejo centralizado de errores | `ManejadorErrores` (`@ControllerAdvice`): 404, 400 y error de base de datos |
| Perfiles de configuración | La clave de MySQL vive en `application-local.properties`, fuera del repositorio |

## Arquitectura

```
co.edu.sena.gtm
├── GtmApplication            Arranque de Spring Boot
├── controlador/              TareaControlador, InicioControlador, ManejadorErrores
├── servicio/                 TareaServicio (reglas de negocio y transacciones)
├── repositorio/              TareaRepositorio, ProyectoRepositorio, UsuarioRepositorio, HistorialRepositorio
├── modelo/                   Entidades, enumeraciones y convertidores
├── dto/                      TareaFormulario (datos del formulario + validaciones)
└── excepcion/                TareaNoEncontradaException, ReglaNegocioException
```

Flujo: navegador → **controlador** → **servicio** → **repositorio** → MySQL → controlador →
**plantilla Thymeleaf** → navegador.

## Reglas de negocio

- Una tarea nueva siempre empieza en estado *Pendiente*.
- La fecha límite no puede ser anterior a hoy al crear una tarea.
- Al editar no se modifican el estado ni el creador.
- No se puede cambiar una tarea al estado en el que ya está.
- Todo cambio de estado exige indicar quién lo realiza y queda en el historial.

## Estándares de codificación (GA7-AA1-EV02)

Paquetes en minúsculas; clases en PascalCase (`TareaServicio`); métodos y variables en camelCase
(`cambiarEstado`, `usuarioId`); constantes en MAYÚSCULAS (`VISTA_FORMULARIO`); Javadoc en las
clases y métodos públicos y comentarios que explican el porqué de las decisiones.

## Relación con los artefactos del proyecto

| Artefacto previo | Uso en este módulo |
|---|---|
| Modelo relacional y script SQL (GA6) | Tablas `tareas`, `proyectos`, `usuarios`, `historial_estados` |
| Prototipos y mockups (GA6 AA3) | Pantallas de lista, formulario y detalle; paleta de colores |
| Requisitos RF-03, RF-04, RF-05 | Crear, editar y consultar tareas; cambiar su estado con historial |
| Módulos JDBC y Servlets (GA7 AA2) | Misma funcionalidad, ahora con framework |
| Plan de versionamiento (GA7 AA1) | Git + GitHub + Conventional Commits |

## Alcance y limitaciones

- No incluye inicio de sesión: los formularios piden elegir el usuario que realiza la acción.
- No cubre la asignación de tareas a usuarios (`tarea_usuario`), comentarios ni adjuntos.
