-- Eliminacion de tablas si existen para evitar conflictos, con orden correcto para evitar errores de claves foraneas
DROP TABLE IF EXISTS presupuestos;
DROP TABLE IF EXISTS registros_tiempo;
DROP TABLE IF EXISTS reunion_participantes;
DROP TABLE IF EXISTS reuniones;
DROP TABLE IF EXISTS tareas;
DROP TABLE IF EXISTS proyecto_empleados;
DROP TABLE IF EXISTS proyectos;
DROP TABLE IF EXISTS rrhh;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS jefes;
DROP TABLE IF EXISTS usuarios;

-- Tabla de usuarios donde heredarán empleados, jefes y RRHH
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
                          contraseña VARCHAR(100) NOT NULL,
                          correo VARCHAR(100) NOT NULL UNIQUE,
                          nombre_completo VARCHAR(100),
                          rol VARCHAR(20) NOT NULL,
                          activo BOOLEAN DEFAULT TRUE,
                          tipo_usuario VARCHAR(20) NOT NULL,

                          -- Validaciones para asegurar datos correctos
                          CONSTRAINT chk_rol CHECK (rol IN ('JEFE', 'EMPLEADO', 'RRHH')),
                          CONSTRAINT chk_tipo_usuario CHECK (tipo_usuario IN ('JEFE', 'EMPLEADO', 'RRHH'))
);

-- Tabla jefes que hereda de usuarios
CREATE TABLE jefes (
                       id BIGINT PRIMARY KEY,
                       fecha_nombramiento DATE,
                       nivel_responsabilidad VARCHAR(50),

                       FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE,
                       CONSTRAINT chk_nivel_responsabilidad CHECK (nivel_responsabilidad IN ('Junior', 'Medio', 'Senior'))
);

-- Tabla empleados que hereda de usuarios
CREATE TABLE empleados (
                           id BIGINT PRIMARY KEY,
                           fecha_contratacion DATE,
                           puesto VARCHAR(100),
                           telefono VARCHAR(20),
                           salario DOUBLE PRECISION,

                           FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE,
                           CONSTRAINT chk_salario CHECK (salario >= 0)
);

-- Tabla RRHH que hereda de usuarios
CREATE TABLE rrhh (
                      id BIGINT PRIMARY KEY,
                      area_especializacion VARCHAR(100),
                      certificaciones VARCHAR(200),
                      fecha_incorporacion_rrhh DATE,

                      FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla proyectos gestionados por jefes
CREATE TABLE proyectos (
                           id BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion TEXT,
                           jefe_id BIGINT,
                           fecha_inicio DATE,
                           fecha_fin_estimada DATE,
                           fecha_fin_real DATE,
                           estado VARCHAR(50) DEFAULT 'PLANIFICACION',
                           presupuesto DOUBLE PRECISION,
                           completado BOOLEAN DEFAULT FALSE,

                           FOREIGN KEY (jefe_id) REFERENCES jefes(id),
                           CONSTRAINT chk_estado_proyecto CHECK (estado IN ('PLANIFICACION', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO')),
                           CONSTRAINT chk_presupuesto CHECK (presupuesto >= 0),
                           CONSTRAINT chk_fechas_proyecto CHECK (fecha_fin_estimada >= fecha_inicio)
);

-- Tabla proyectos-empleados (relacion muchos a muchos)
CREATE TABLE proyecto_empleados (
                                    proyecto_id BIGINT,
                                    empleado_id BIGINT,

                                    PRIMARY KEY (proyecto_id, empleado_id),
                                    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) ON DELETE CASCADE,
                                    FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE
);

-- Tabla tareas asignadas por jefes a empleados
CREATE TABLE tareas (
                        id BIGSERIAL PRIMARY KEY,
                        titulo VARCHAR(100) NOT NULL,
                        descripcion TEXT,
                        empleado_id BIGINT,
                        jefe_id BIGINT,
                        proyecto_id BIGINT,
                        fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        fecha_vencimiento TIMESTAMP,
                        completada BOOLEAN DEFAULT FALSE,
                        fecha_completada TIMESTAMP,
                        prioridad VARCHAR(20) DEFAULT 'MEDIA',

                        FOREIGN KEY (empleado_id) REFERENCES empleados(id),
                        FOREIGN KEY (jefe_id) REFERENCES jefes(id),
                        FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
                        CONSTRAINT chk_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA')),
                        CONSTRAINT chk_fechas_tarea CHECK (fecha_vencimiento >= fecha_creacion)
);

-- Tabla reuniones organizadas por jefes
CREATE TABLE reuniones (
                           id BIGSERIAL PRIMARY KEY,
                           titulo VARCHAR(100) NOT NULL,
                           descripcion TEXT,
                           fecha_hora TIMESTAMP,
                           sala VARCHAR(50),
                           organizador_id BIGINT,
                           completada BOOLEAN DEFAULT FALSE,
                           fecha_completada TIMESTAMP,

                           FOREIGN KEY (organizador_id) REFERENCES jefes(id)
);

-- Tabla reuniones-participantes (relacion muchos a muchos)
CREATE TABLE reunion_participantes (
                                       reunion_id BIGINT,
                                       usuario_id BIGINT,

                                       PRIMARY KEY (reunion_id, usuario_id),
                                       FOREIGN KEY (reunion_id) REFERENCES reuniones(id) ON DELETE CASCADE,
                                       FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla registros-tiempo para tracking de horas trabajadas
CREATE TABLE registros_tiempo (
                                  id BIGSERIAL PRIMARY KEY,
                                  usuario_id BIGINT,
                                  tarea_id BIGINT,
                                  proyecto_id BIGINT,
                                  reunion_id BIGINT,
                                  fecha_registro DATE DEFAULT CURRENT_DATE,
                                  hora_inicio TIMESTAMP,
                                  hora_fin TIMESTAMP,
                                  horas_trabajadas DOUBLE PRECISION,
                                  comentario TEXT,

                                  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                                  FOREIGN KEY (tarea_id) REFERENCES tareas(id),
                                  FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
                                  FOREIGN KEY (reunion_id) REFERENCES reuniones(id),
                                  CONSTRAINT chk_horas_trabajadas CHECK (horas_trabajadas >= 0 AND horas_trabajadas <= 24),
                                  CONSTRAINT chk_horas_registro CHECK (hora_fin > hora_inicio)
);

-- Tabla presupuestos creados por RRHH para proyectos
CREATE TABLE presupuestos (
                              id BIGSERIAL PRIMARY KEY,
                              nombre_cliente VARCHAR(100) NOT NULL,
                              proyecto_id BIGINT,
                              rrhh_id BIGINT,
                              fecha_creacion DATE DEFAULT CURRENT_DATE,
                              tarifa_hora DOUBLE PRECISION,
                              horas_estimadas DOUBLE PRECISION,
                              costo_total DOUBLE PRECISION,
                              descripcion TEXT,
                              estado VARCHAR(20) DEFAULT 'BORRADOR',

                              FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
                              FOREIGN KEY (rrhh_id) REFERENCES usuarios(id), -- Asumiendo que rrhh_id es un usuario
                              CONSTRAINT chk_estado_presupuesto CHECK (estado IN ('BORRADOR', 'ENVIADO', 'APROBADO', 'RECHAZADO')),
                              CONSTRAINT chk_tarifa_hora CHECK (tarifa_hora >= 0),
                              CONSTRAINT chk_horas_estimadas CHECK (horas_estimadas >= 0),
                              CONSTRAINT chk_costo_total CHECK (costo_total >= 0)
);
