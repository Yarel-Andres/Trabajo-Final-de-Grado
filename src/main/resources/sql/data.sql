-- Insertar usuarios base (jefes)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('jefe1', 'password', 'jefe1@empresa.com', 'Antonio García', 'JEFE', true, 'JEFE'),
    ('jefe2', 'password', 'jefe2@empresa.com', 'María López', 'JEFE', true, 'JEFE');

-- Insertar jefes (usando SELECT para obtener los IDs de los usuarios recién insertados)
INSERT INTO jefes (id, fecha_nombramiento, nivel_responsabilidad)
SELECT id, '2022-01-15', 'Senior' FROM usuarios WHERE nombre_usuario = 'jefe1'
UNION ALL
SELECT id, '2023-03-10', 'Medio' FROM usuarios WHERE nombre_usuario = 'jefe2';

-- Insertar usuarios base (empleados)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('empleado1', 'password', 'empleado1@empresa.com', 'Carlos Martínez', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado2', 'password', 'empleado2@empresa.com', 'Laura Sánchez', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado3', 'password', 'empleado3@empresa.com', 'David Fernández', 'EMPLEADO', true, 'EMPLEADO');

-- Insertar empleados (usando SELECT para obtener los IDs de los usuarios recién insertados)
INSERT INTO empleados (id, fecha_contratacion, puesto, telefono, salario)
SELECT id, '2023-02-20', 'Desarrollador', '666111222', 32000.0 FROM usuarios WHERE nombre_usuario = 'empleado1'
UNION ALL
SELECT id, '2022-11-05', 'Diseñador UX', '666333444', 29000.0 FROM usuarios WHERE nombre_usuario = 'empleado2'
UNION ALL
SELECT id, '2023-06-15', 'Analista', '666555666', 27500.0 FROM usuarios WHERE nombre_usuario = 'empleado3';

-- Insertar usuarios base (RRHH)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('rrhh1', 'password', 'rrhh1@empresa.com', 'Elena Ruiz', 'RRHH', true, 'RRHH');

-- Insertar RRHH (usando SELECT para obtener el ID del usuario recién insertado)
INSERT INTO rrhh (id, area_especializacion, certificaciones, fecha_incorporacion_rrhh)
SELECT id, 'Contratación', 'CIPD Level 5', '2022-05-12' FROM usuarios WHERE nombre_usuario = 'rrhh1';

-- Insertar proyectos
INSERT INTO proyectos (nombre, descripcion, jefe_id, fecha_inicio, fecha_fin_estimada, estado)
SELECT 'Sistema de Gestión', 'Sistema para gestión empresarial', id, '2023-01-20', '2023-12-31', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1')
UNION ALL
SELECT 'App Móvil', 'Aplicación móvil para clientes', id, '2023-05-15', '2023-11-30', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe2');

-- Insertar tareas (usando subqueries para obtener los IDs)
INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad)
SELECT
    'Desarrollar API',
    'Implementar endpoints de la API REST',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado1'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1'),
    NOW() - INTERVAL 5 DAY,
    CURDATE() + INTERVAL 10 DAY,
    false,
    'MEDIA';

INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad)
SELECT
    'Diseñar interfaz',
    'Diseñar la interfaz principal del sistema',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado2'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1'),
    NOW() - INTERVAL 7 DAY,
    CURDATE() + INTERVAL 5 DAY,
    false,
    'ALTA';

INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, fecha_completada, prioridad)
SELECT
    'Análisis de requisitos',
    'Analizar requisitos del cliente',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado3'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe2'),
    NOW() - INTERVAL 10 DAY,
    CURDATE() - INTERVAL 3 DAY,
    true,
    NOW() - INTERVAL 1 DAY,
    'BAJA';

-- Insertar reuniones
INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id)
SELECT
    'Kickoff proyecto',
    'Reunión inicial del proyecto',
    NOW() + INTERVAL 3 DAY + INTERVAL 10 HOUR,
    'Sala A',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1');

INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id)
SELECT
    'Revisión de avances',
    'Revisión semanal de avances',
    NOW() + INTERVAL 7 DAY + INTERVAL 11 HOUR,
    'Sala B',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe2');

-- Insertar participantes de reuniones (ahora usando subqueries para los IDs)
INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Kickoff proyecto'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Kickoff proyecto'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado1');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Kickoff proyecto'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado2');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Revisión de avances'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe2');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Revisión de avances'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado3');

-- Insertar registros de tiempo
INSERT INTO registros_tiempo (tarea_id, fecha_registro, hora_inicio, hora_fin, horas_trabajadas, comentario)
SELECT
    (SELECT id FROM tareas WHERE titulo = 'Desarrollar API'),
    CURDATE() - INTERVAL 2 DAY,
    NOW() - INTERVAL 26 HOUR,
    NOW() - INTERVAL 22 HOUR,
    4.0,
    'Avance en la implementación de la API';

INSERT INTO registros_tiempo (tarea_id, fecha_registro, hora_inicio, hora_fin, horas_trabajadas, comentario)
SELECT
    (SELECT id FROM tareas WHERE titulo = 'Diseñar interfaz'),
    CURDATE() - INTERVAL 1 DAY,
    NOW() - INTERVAL 25 HOUR,
    NOW() - INTERVAL 21 HOUR,
    4.0,
    'Diseño de la interfaz principal';