-- Eliminar tablas si existen para evitar conflictos
DROP TABLE IF EXISTS proyecto_empleados;
DROP TABLE IF EXISTS registros_tiempo;
DROP TABLE IF EXISTS reunion_participantes;
DROP TABLE IF EXISTS reuniones;
DROP TABLE IF EXISTS tareas;
DROP TABLE IF EXISTS proyectos;
DROP TABLE IF EXISTS rrhh;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS jefes;
DROP TABLE IF EXISTS usuarios;

-- Crear tabla de usuarios (clase base)
CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
                          contraseña VARCHAR(100) NOT NULL,
                          correo VARCHAR(100) NOT NULL UNIQUE,
                          nombre_completo VARCHAR(100),
                          rol VARCHAR(20) NOT NULL,
                          activo BOOLEAN DEFAULT TRUE,
                          tipo_usuario VARCHAR(20) NOT NULL
);

-- Crear tabla de jefes
CREATE TABLE jefes (
                       id BIGINT PRIMARY KEY,
                       fecha_nombramiento DATE,
                       nivel_responsabilidad VARCHAR(50),
                       FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- Crear tabla de empleados
CREATE TABLE empleados (
                           id BIGINT PRIMARY KEY,
                           fecha_contratacion DATE,
                           puesto VARCHAR(100),
                           telefono VARCHAR(20),
                           salario DOUBLE,
                           FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- Crear tabla de RRHH
CREATE TABLE rrhh (
                      id BIGINT PRIMARY KEY,
                      area_especializacion VARCHAR(100),
                      certificaciones VARCHAR(200),
                      fecha_incorporacion_rrhh DATE,
                      FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- Crear tabla de proyectos
CREATE TABLE proyectos (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion TEXT,
                           jefe_id BIGINT,
                           fecha_inicio DATE,
                           fecha_fin_estimada DATE,
                           fecha_fin_real DATE,
                           estado VARCHAR(50),
                           presupuesto DOUBLE,
                           FOREIGN KEY (jefe_id) REFERENCES jefes(id)
);

-- Crear tabla de relación entre proyectos y empleados
CREATE TABLE proyecto_empleados (
                                    proyecto_id BIGINT,
                                    empleado_id BIGINT,
                                    PRIMARY KEY (proyecto_id, empleado_id),
                                    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
                                    FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

-- Crear tabla de tareas
CREATE TABLE tareas (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        titulo VARCHAR(100) NOT NULL,
                        descripcion TEXT,
                        empleado_id BIGINT,
                        jefe_id BIGINT,
                        proyecto_id BIGINT, -- Nueva columna para la relación con proyecto
                        fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                        fecha_vencimiento DATE,
                        completada BOOLEAN DEFAULT FALSE,
                        fecha_completada DATETIME,
                        prioridad VARCHAR(20),
                        FOREIGN KEY (empleado_id) REFERENCES empleados(id),
                        FOREIGN KEY (jefe_id) REFERENCES jefes(id),
                        FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) -- Nueva clave foránea
);

-- Crear tabla de reuniones
CREATE TABLE reuniones (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           titulo VARCHAR(100) NOT NULL,
                           descripcion TEXT,
                           fecha_hora DATETIME,
                           sala VARCHAR(50),
                           organizador_id BIGINT,
                           FOREIGN KEY (organizador_id) REFERENCES jefes(id)
);

-- Crear tabla de participantes de reuniones (relación muchos a muchos)
CREATE TABLE reunion_participantes (
                                       reunion_id BIGINT,
                                       usuario_id BIGINT,
                                       PRIMARY KEY (reunion_id, usuario_id),
                                       FOREIGN KEY (reunion_id) REFERENCES reuniones(id),
                                       FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Crear tabla de registros de tiempo
CREATE TABLE registros_tiempo (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  usuario_id BIGINT,
                                  tarea_id BIGINT,
                                  proyecto_id BIGINT,
                                  reunion_id BIGINT,
                                  fecha_registro DATE DEFAULT (CURRENT_DATE),
                                  hora_inicio DATETIME,
                                  hora_fin DATETIME,
                                  horas_trabajadas DOUBLE,
                                  comentario TEXT,
                                  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                                  FOREIGN KEY (tarea_id) REFERENCES tareas(id),
                                  FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
                                  FOREIGN KEY (reunion_id) REFERENCES reuniones(id)
);
