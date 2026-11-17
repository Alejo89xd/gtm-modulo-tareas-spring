-- ═══════════════════════════════════════════════════════════════════════
-- GA6-220501096-AA2-EV03 — Script Modelo Físico Completo
-- Sistema de Gestión de Tareas (GTM)
-- Aprendiz: _______________________________
-- Ficha: _______________________________
-- Instructor: _______________________________
-- Motor: MySQL 8.0 · Engine: InnoDB · Charset: utf8mb4
-- ═══════════════════════════════════════════════════════════════════════

-- ── CONFIGURACIÓN INICIAL ────────────────────────────────────────────
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ── CREAR BASE DE DATOS ──────────────────────────────────────────────
DROP DATABASE IF EXISTS gtm_db;
CREATE DATABASE gtm_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gtm_db;

-- ════════════════════════════════════════════════════════════════════
-- SECCIÓN 1: DDL — CREACIÓN DE TABLAS Y RESTRICCIONES
-- ════════════════════════════════════════════════════════════════════

-- ── TABLA: roles ─────────────────────────────────────────────────────
-- Catálogo de roles del sistema. Controla el nivel de acceso.
CREATE TABLE roles (
  id          INT         NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del rol',
  nombre      VARCHAR(50) NOT NULL                COMMENT 'Nombre del rol',
  descripcion TEXT        NULL                    COMMENT 'Descripción de responsabilidades',
  PRIMARY KEY (id),
  UNIQUE KEY uq_roles_nombre (nombre)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de roles del sistema GTM';

-- ── TABLA: usuarios ──────────────────────────────────────────────────
-- Almacena todos los actores del sistema con su rol asignado.
CREATE TABLE usuarios (
  id              INT          NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del usuario',
  rol_id          INT          NOT NULL                COMMENT 'FK: rol asignado al usuario',
  nombre          VARCHAR(100) NOT NULL                COMMENT 'Nombre(s) del usuario',
  apellido        VARCHAR(100) NOT NULL                COMMENT 'Apellido(s) del usuario',
  cedula          VARCHAR(20)  NOT NULL                COMMENT 'Número de documento de identidad',
  correo          VARCHAR(150) NOT NULL                COMMENT 'Correo electrónico para login',
  contrasena_hash VARCHAR(255) NOT NULL                COMMENT 'Hash bcrypt de la contraseña',
  activo          BOOLEAN      NOT NULL DEFAULT TRUE   COMMENT 'Indica si el usuario está habilitado',
  fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de registro',
  PRIMARY KEY (id),
  UNIQUE KEY uq_usuarios_cedula (cedula),
  UNIQUE KEY uq_usuarios_correo (correo),
  INDEX idx_usuarios_rol (rol_id),
  CONSTRAINT fk_usuarios_rol
    FOREIGN KEY (rol_id) REFERENCES roles(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Usuarios del sistema GTM';

-- ── TABLA: proyectos ─────────────────────────────────────────────────
-- Proyectos de software gestionados en el sistema.
CREATE TABLE proyectos (
  id                 INT          NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del proyecto',
  jefe_id            INT          NOT NULL                COMMENT 'FK: jefe de proyecto responsable',
  cliente_id         INT          NULL                    COMMENT 'FK: cliente corporativo (opcional)',
  nombre             VARCHAR(150) NOT NULL                COMMENT 'Nombre del proyecto',
  descripcion        TEXT         NULL                    COMMENT 'Descripción del proyecto',
  fecha_inicio       DATE         NOT NULL                COMMENT 'Fecha de inicio',
  fecha_fin_estimada DATE         NULL                    COMMENT 'Fecha estimada de entrega',
  estado             ENUM('activo','pausado','completado','cancelado')
                                  NOT NULL DEFAULT 'activo' COMMENT 'Estado actual del proyecto',
  fecha_creacion     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación',
  PRIMARY KEY (id),
  INDEX idx_proyectos_jefe   (jefe_id),
  INDEX idx_proyectos_cliente(cliente_id),
  INDEX idx_proyectos_estado (estado),
  CONSTRAINT fk_proyectos_jefe
    FOREIGN KEY (jefe_id)    REFERENCES usuarios(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT fk_proyectos_cliente
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Proyectos gestionados en GTM';

-- ── TABLA: tareas ─────────────────────────────────────────────────────
-- Unidad central del sistema. Cada tarea pertenece a un proyecto.
CREATE TABLE tareas (
  id                  INT          NOT NULL AUTO_INCREMENT COMMENT 'Identificador único de la tarea',
  proyecto_id         INT          NOT NULL                COMMENT 'FK: proyecto al que pertenece',
  creador_id          INT          NOT NULL                COMMENT 'FK: usuario que creó la tarea',
  titulo              VARCHAR(200) NOT NULL                COMMENT 'Título descriptivo de la tarea',
  descripcion         TEXT         NULL                    COMMENT 'Descripción detallada',
  prioridad           ENUM('alta','media','baja')
                                   NOT NULL DEFAULT 'media' COMMENT 'Nivel de prioridad',
  estado              ENUM('pendiente','en_progreso','en_revision','completada')
                                   NOT NULL DEFAULT 'pendiente' COMMENT 'Estado actual',
  fecha_limite        DATE         NULL                    COMMENT 'Fecha límite de entrega',
  fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación',
  fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última actualización',
  PRIMARY KEY (id),
  INDEX idx_tareas_proyecto (proyecto_id),
  INDEX idx_tareas_creador  (creador_id),
  INDEX idx_tareas_estado   (estado),
  INDEX idx_tareas_prioridad(prioridad),
  CONSTRAINT fk_tareas_proyecto
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_tareas_creador
    FOREIGN KEY (creador_id)  REFERENCES usuarios(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Tareas del sistema GTM';

-- ── TABLA: tarea_usuario (N:M) ────────────────────────────────────────
-- Tabla intermedia: asignación de tareas a usuarios (muchos a muchos).
CREATE TABLE tarea_usuario (
  tarea_id         INT       NOT NULL COMMENT 'FK: tarea asignada (parte PK compuesta)',
  usuario_id       INT       NOT NULL COMMENT 'FK: usuario asignado (parte PK compuesta)',
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de asignación',
  activo           BOOLEAN   NOT NULL DEFAULT TRUE COMMENT 'Permite desasignar sin borrar',
  PRIMARY KEY (tarea_id, usuario_id),
  INDEX idx_tu_usuario (usuario_id),
  CONSTRAINT fk_tu_tarea
    FOREIGN KEY (tarea_id)   REFERENCES tareas(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_tu_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Asignación N:M de tareas a usuarios';

-- ── TABLA: comentarios ────────────────────────────────────────────────
-- Comentarios realizados por usuarios sobre tareas específicas.
CREATE TABLE comentarios (
  id             INT       NOT NULL AUTO_INCREMENT COMMENT 'Identificador único',
  tarea_id       INT       NOT NULL                COMMENT 'FK: tarea comentada',
  autor_id       INT       NULL                    COMMENT 'FK: autor del comentario',
  contenido      TEXT      NOT NULL                COMMENT 'Texto del comentario',
  editado        BOOLEAN   NOT NULL DEFAULT FALSE  COMMENT 'Si fue editado posteriormente',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha del comentario',
  PRIMARY KEY (id),
  INDEX idx_comentarios_tarea (tarea_id),
  INDEX idx_comentarios_autor (autor_id),
  CONSTRAINT fk_comentarios_tarea
    FOREIGN KEY (tarea_id) REFERENCES tareas(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_comentarios_autor
    FOREIGN KEY (autor_id) REFERENCES usuarios(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Comentarios en tareas';

-- ── TABLA: notificaciones ─────────────────────────────────────────────
-- Notificaciones automáticas enviadas a usuarios ante eventos.
CREATE TABLE notificaciones (
  id          INT       NOT NULL AUTO_INCREMENT COMMENT 'Identificador único',
  usuario_id  INT       NOT NULL                COMMENT 'FK: destinatario',
  tarea_id    INT       NULL                    COMMENT 'FK: tarea relacionada (opcional)',
  mensaje     TEXT      NOT NULL                COMMENT 'Contenido del mensaje',
  tipo        ENUM('cambio_estado','asignacion','comentario','sistema')
                        NOT NULL                COMMENT 'Categoría de la notificación',
  leida       BOOLEAN   NOT NULL DEFAULT FALSE  COMMENT 'Si el usuario la leyó',
  fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de generación',
  PRIMARY KEY (id),
  INDEX idx_notif_usuario (usuario_id),
  INDEX idx_notif_tarea   (tarea_id),
  INDEX idx_notif_leida   (leida),
  CONSTRAINT fk_notif_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_notif_tarea
    FOREIGN KEY (tarea_id)   REFERENCES tareas(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Notificaciones del sistema';

-- ── TABLA: archivos_adjuntos ──────────────────────────────────────────
-- Archivos subidos como soporte a una tarea.
CREATE TABLE archivos_adjuntos (
  id                  INT          NOT NULL AUTO_INCREMENT COMMENT 'Identificador único',
  tarea_id            INT          NOT NULL                COMMENT 'FK: tarea propietaria',
  subido_por          INT          NOT NULL                COMMENT 'FK: usuario que lo subió',
  nombre_archivo      VARCHAR(255) NOT NULL                COMMENT 'Nombre original del archivo',
  ruta_almacenamiento TEXT         NOT NULL                COMMENT 'Ruta o URL de almacenamiento',
  tamanio_bytes       BIGINT       NOT NULL                COMMENT 'Tamaño en bytes',
  tipo_mime           VARCHAR(100) NOT NULL                COMMENT 'MIME type del archivo',
  fecha_subida        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de carga',
  PRIMARY KEY (id),
  INDEX idx_adj_tarea   (tarea_id),
  INDEX idx_adj_usuario (subido_por),
  CONSTRAINT fk_adjuntos_tarea
    FOREIGN KEY (tarea_id)   REFERENCES tareas(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_adjuntos_usuario
    FOREIGN KEY (subido_por) REFERENCES usuarios(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Archivos adjuntos de tareas';

-- ── TABLA: historial_estados ──────────────────────────────────────────
-- Registro cronológico de cambios de estado en las tareas.
CREATE TABLE historial_estados (
  id              INT       NOT NULL AUTO_INCREMENT COMMENT 'Identificador único',
  tarea_id        INT       NOT NULL                COMMENT 'FK: tarea modificada',
  cambiado_por    INT       NOT NULL                COMMENT 'FK: usuario que hizo el cambio',
  estado_anterior ENUM('pendiente','en_progreso','en_revision','completada')
                            NULL                    COMMENT 'Estado previo',
  estado_nuevo    ENUM('pendiente','en_progreso','en_revision','completada')
                            NOT NULL                COMMENT 'Estado nuevo',
  observacion     TEXT      NULL                    COMMENT 'Motivo del cambio (opcional)',
  fecha_cambio    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha del cambio',
  PRIMARY KEY (id),
  INDEX idx_hist_tarea   (tarea_id),
  INDEX idx_hist_usuario (cambiado_por),
  CONSTRAINT fk_historial_tarea
    FOREIGN KEY (tarea_id)     REFERENCES tareas(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_historial_usuario
    FOREIGN KEY (cambiado_por) REFERENCES usuarios(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Historial de cambios de estado en tareas';

-- ════════════════════════════════════════════════════════════════════
-- SECCIÓN 2: DML — DATOS DE PRUEBA (SEED DATA)
-- ════════════════════════════════════════════════════════════════════

-- ── INSERT: roles ────────────────────────────────────────────────────
INSERT INTO roles (nombre, descripcion) VALUES
  ('Administrador', 'Acceso total al sistema. Gestiona usuarios y configuración global.'),
  ('Jefe de Proyecto', 'Crea y gestiona proyectos, asigna tareas y genera reportes.'),
  ('Desarrollador', 'Ejecuta tareas asignadas y actualiza su estado de progreso.'),
  ('Cliente Corporativo', 'Consulta el avance de sus proyectos y genera reportes de seguimiento.');

-- ── INSERT: usuarios ─────────────────────────────────────────────────
INSERT INTO usuarios (rol_id, nombre, apellido, cedula, correo, contrasena_hash) VALUES
  (1, 'Sistema',   'Administrador', '0000000001', 'admin@gtm.com',       '$2b$12$adminHashExample0001'),
  (2, 'Carlos',    'Ramirez',       '1010101010', 'carlos.r@gtm.com',    '$2b$12$jefeHashExample00001'),
  (2, 'Diana',     'Lopez',         '2020202020', 'diana.l@gtm.com',     '$2b$12$jefeHashExample00002'),
  (3, 'Juan',      'Martinez',      '3030303030', 'juan.m@gtm.com',      '$2b$12$devHashExample000001'),
  (3, 'Ana',       'Gonzalez',      '4040404040', 'ana.g@gtm.com',       '$2b$12$devHashExample000002'),
  (3, 'Pedro',     'Suarez',        '5050505050', 'pedro.s@gtm.com',     '$2b$12$devHashExample000003'),
  (4, 'TechCorp',  'S.A.S.',        '9001234567', 'contacto@techcorp.co','$2b$12$clienteHashExample01'),
  (4, 'InnoVentas','Ltda.',         '9007654321', 'info@innoventas.co',  '$2b$12$clienteHashExample02');

-- ── INSERT: proyectos ────────────────────────────────────────────────
INSERT INTO proyectos (jefe_id, cliente_id, nombre, descripcion, fecha_inicio, fecha_fin_estimada, estado) VALUES
  (2, 7, 'Sistema de Gestión de Tareas',
   'Desarrollo de aplicación web para gestión colaborativa de tareas y proyectos.',
   '2024-01-15', '2024-07-31', 'activo'),
  (3, 8, 'Portal de Ventas Online',
   'E-commerce con carrito de compras, pagos en línea y panel administrativo.',
   '2024-03-01', '2024-09-30', 'activo'),
  (2, 7, 'App Móvil de Notificaciones',
   'Aplicación Android/iOS para gestión de alertas del sistema GTM.',
   '2024-06-01', '2024-12-15', 'activo');

-- ── INSERT: tareas ───────────────────────────────────────────────────
INSERT INTO tareas (proyecto_id, creador_id, titulo, descripcion, prioridad, estado, fecha_limite) VALUES
  (1, 2, 'Implementar autenticación JWT',
   'Configurar el middleware de autenticación con JWT y refresh tokens en Express.',
   'alta', 'en_progreso', '2024-02-28'),
  (1, 2, 'Diseñar modelo de base de datos',
   'Crear el modelo relacional normalizado hasta 3FN con todas las restricciones.',
   'alta', 'completada', '2024-01-31'),
  (1, 2, 'Desarrollar API REST de tareas',
   'Implementar los endpoints CRUD para la gestión de tareas con validación.',
   'alta', 'pendiente', '2024-03-15'),
  (1, 2, 'Configurar Docker Compose',
   'Crear el archivo docker-compose.yml con los servicios Node, PostgreSQL y Redis.',
   'media', 'en_progreso', '2024-02-20'),
  (1, 3, 'Maquetar interfaz del Kanban',
   'Desarrollar el tablero Kanban en React con drag and drop y filtros.',
   'media', 'pendiente', '2024-03-30'),
  (2, 3, 'Configurar pasarela de pagos',
   'Integrar Stripe/PayU para procesamiento de pagos seguros.',
   'alta', 'pendiente', '2024-04-15'),
  (2, 3, 'Diseñar catálogo de productos',
   'Crear módulo de gestión de productos con imágenes, categorías y stock.',
   'media', 'en_progreso', '2024-04-01'),
  (3, 2, 'Configurar Firebase Push Notifications',
   'Integrar Firebase Cloud Messaging para notificaciones push en iOS y Android.',
   'alta', 'pendiente', '2024-07-01');

-- ── INSERT: tarea_usuario ────────────────────────────────────────────
INSERT INTO tarea_usuario (tarea_id, usuario_id) VALUES
  (1, 4),  -- Juan → Implementar autenticación
  (2, 5),  -- Ana  → Diseñar modelo BD
  (3, 4),  -- Juan → Desarrollar API REST
  (3, 6),  -- Pedro → Desarrollar API REST
  (4, 6),  -- Pedro → Docker Compose
  (5, 5),  -- Ana  → Kanban
  (6, 4),  -- Juan → Pasarela de pagos
  (7, 5),  -- Ana  → Catálogo de productos
  (7, 6),  -- Pedro → Catálogo de productos
  (8, 4);  -- Juan → Firebase

-- ── INSERT: comentarios ──────────────────────────────────────────────
INSERT INTO comentarios (tarea_id, autor_id, contenido) VALUES
  (1, 4, 'El middleware ya está configurado. Pendiente implementar el refresh token endpoint.'),
  (1, 2, 'Recuerda manejar correctamente la expiración del token y los errores 401/403.'),
  (2, 5, 'El modelo ya fue validado con el instructor. Todas las tablas cumplen 3FN.'),
  (4, 6, 'El contenedor de PostgreSQL levanta correctamente. Falta configurar el volumen persistente.'),
  (5, 5, 'Investigando la librería react-beautiful-dnd para el drag and drop del Kanban.');

-- ── INSERT: notificaciones ───────────────────────────────────────────
INSERT INTO notificaciones (usuario_id, tarea_id, mensaje, tipo) VALUES
  (4, 1, 'Se te ha asignado la tarea: Implementar autenticación JWT', 'asignacion'),
  (5, 2, 'Se te ha asignado la tarea: Diseñar modelo de base de datos', 'asignacion'),
  (4, 3, 'Se te ha asignado la tarea: Desarrollar API REST de tareas', 'asignacion'),
  (6, 3, 'Se te ha asignado la tarea: Desarrollar API REST de tareas', 'asignacion'),
  (4, 1, 'Nuevo comentario en tu tarea: Implementar autenticación JWT', 'comentario'),
  (2, 2, 'La tarea "Diseñar modelo de base de datos" cambió a: completada', 'cambio_estado');

-- ── INSERT: historial_estados ────────────────────────────────────────
INSERT INTO historial_estados (tarea_id, cambiado_por, estado_anterior, estado_nuevo, observacion) VALUES
  (1, 4, 'pendiente',    'en_progreso', 'Iniciando configuración del middleware JWT en Express.'),
  (2, 5, 'pendiente',    'en_progreso', 'Comenzando el diseño del modelo entidad-relación.'),
  (2, 5, 'en_progreso',  'en_revision', 'Modelo terminado. Pendiente validación del instructor.'),
  (2, 2, 'en_revision',  'completada',  'Modelo aprobado. Cumple todas las formas normales.'),
  (4, 6, 'pendiente',    'en_progreso', 'Iniciando configuración de docker-compose.yml.'),
  (7, 5, 'pendiente',    'en_progreso', 'Comenzando el diseño del catálogo de productos.');

-- ════════════════════════════════════════════════════════════════════
-- SECCIÓN 3: VISTAS — CONSULTAS FRECUENTES
-- ════════════════════════════════════════════════════════════════════

-- Vista: tareas con información completa
CREATE VIEW v_tareas_completas AS
SELECT
  t.id,
  t.titulo,
  t.prioridad,
  t.estado,
  t.fecha_limite,
  p.nombre        AS proyecto,
  CONCAT(u.nombre, ' ', u.apellido) AS creador,
  t.fecha_creacion
FROM tareas t
JOIN proyectos p ON t.proyecto_id = p.id
JOIN usuarios  u ON t.creador_id  = u.id;

-- Vista: tareas por desarrollador
CREATE VIEW v_mis_tareas AS
SELECT
  CONCAT(u.nombre, ' ', u.apellido) AS desarrollador,
  t.titulo,
  t.prioridad,
  t.estado,
  t.fecha_limite,
  p.nombre AS proyecto
FROM tarea_usuario tu
JOIN usuarios  u ON tu.usuario_id  = u.id
JOIN tareas    t ON tu.tarea_id    = t.id
JOIN proyectos p ON t.proyecto_id  = p.id
WHERE tu.activo = TRUE;

-- Vista: resumen de proyectos
CREATE VIEW v_resumen_proyectos AS
SELECT
  p.id,
  p.nombre                                          AS proyecto,
  p.estado,
  COUNT(DISTINCT t.id)                              AS total_tareas,
  SUM(t.estado = 'completada')                      AS completadas,
  SUM(t.estado = 'en_progreso')                     AS en_progreso,
  SUM(t.estado = 'pendiente')                       AS pendientes,
  CONCAT(u.nombre, ' ', u.apellido)                AS jefe
FROM proyectos p
LEFT JOIN tareas    t ON p.id       = t.proyecto_id
LEFT JOIN usuarios  u ON p.jefe_id  = u.id
GROUP BY p.id, p.nombre, p.estado, u.nombre, u.apellido;

-- ════════════════════════════════════════════════════════════════════
-- SECCIÓN 4: VERIFICACIÓN FINAL
-- ════════════════════════════════════════════════════════════════════

-- Mostrar todas las tablas creadas
SHOW TABLES;

-- Verificar datos insertados
SELECT 'roles'              AS tabla, COUNT(*) AS registros FROM roles
UNION ALL
SELECT 'usuarios',          COUNT(*) FROM usuarios
UNION ALL
SELECT 'proyectos',         COUNT(*) FROM proyectos
UNION ALL
SELECT 'tareas',            COUNT(*) FROM tareas
UNION ALL
SELECT 'tarea_usuario',     COUNT(*) FROM tarea_usuario
UNION ALL
SELECT 'comentarios',       COUNT(*) FROM comentarios
UNION ALL
SELECT 'notificaciones',    COUNT(*) FROM notificaciones
UNION ALL
SELECT 'archivos_adjuntos', COUNT(*) FROM archivos_adjuntos
UNION ALL
SELECT 'historial_estados', COUNT(*) FROM historial_estados;

-- Consulta de prueba: ver tareas completas
SELECT * FROM v_tareas_completas;

-- Consulta de prueba: resumen de proyectos
SELECT * FROM v_resumen_proyectos;

-- ── RESTAURAR CONFIGURACIÓN ──────────────────────────────────────────
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ════════════════════════════════════════════════════════════════════
-- FIN DEL SCRIPT — GA6-220501096-AA2-EV03
-- Sistema de Gestión de Tareas (GTM) — SENA
-- ════════════════════════════════════════════════════════════════════
