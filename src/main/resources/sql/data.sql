-- Insertar usuarios base (jefes)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('jefe1', 'pass', 'jefe1@empresa.com', 'Yarel', 'JEFE', true, 'JEFE'),
    ('jefe2', 'pass', 'jefe2@empresa.com', 'Amado', 'JEFE', true, 'JEFE'),
    ('jefe3', 'pass', 'jefe3@empresa.com', 'Moises', 'JEFE', true, 'JEFE'),
    ('jefe4', 'pass', 'jefe4@empresa.com', 'Zacarias', 'JEFE', true, 'JEFE'),
    ('jefe5', 'pass', 'jefe5@empresa.com', 'Efren', 'JEFE', true, 'JEFE');

-- Insertar jefes (usando SELECT para obtener los IDs de los usuarios recién insertados)
INSERT INTO jefes (id, fecha_nombramiento, nivel_responsabilidad)
SELECT id, '2022-01-15', 'Senior' FROM usuarios WHERE nombre_usuario = 'jefe1'
UNION ALL
SELECT id, '2023-03-10', 'Medio' FROM usuarios WHERE nombre_usuario = 'jefe2'
UNION ALL
SELECT id, '2021-05-20', 'Senior' FROM usuarios WHERE nombre_usuario = 'jefe3'
UNION ALL
SELECT id, '2022-08-15', 'Junior' FROM usuarios WHERE nombre_usuario = 'jefe4'
UNION ALL
SELECT id, '2023-01-05', 'Medio' FROM usuarios WHERE nombre_usuario = 'jefe5';

-- Insertar usuarios base (empleados)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('empleado1', 'pass', 'empleado1@empresa.com', 'Edson', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado2', 'pass', 'empleado2@empresa.com', 'Jose', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado3', 'pass', 'empleado3@empresa.com', 'Ramiro', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado4', 'pass', 'empleado4@empresa.com', 'Raul', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado5', 'pass', 'empleado5@empresa.com', 'Erick', 'EMPLEADO', true, 'EMPLEADO'),
    ('empleado6', 'pass', 'empleado6@empresa.com', 'Jaime', 'EMPLEADO', true, 'EMPLEADO');

-- Insertar empleados (usando SELECT para obtener los IDs de los usuarios recién insertados)
INSERT INTO empleados (id, fecha_contratacion, puesto, telefono, salario)
SELECT id, '2023-02-20', 'Desarrollador', '666111222', 32000.0 FROM usuarios WHERE nombre_usuario = 'empleado1'
UNION ALL
SELECT id, '2022-11-05', 'Diseñador UX', '666333444', 29000.0 FROM usuarios WHERE nombre_usuario = 'empleado2'
UNION ALL
SELECT id, '2023-06-15', 'Analista', '666555666', 27500.0 FROM usuarios WHERE nombre_usuario = 'empleado3'
UNION ALL
SELECT id, '2023-03-10', 'Desarrollador Frontend', '666777888', 31000.0 FROM usuarios WHERE nombre_usuario = 'empleado4'
UNION ALL
SELECT id, '2022-09-01', 'Desarrollador Backend', '666999000', 33000.0 FROM usuarios WHERE nombre_usuario = 'empleado5'
UNION ALL
SELECT id, '2023-04-05', 'QA Tester', '666123456', 28500.0 FROM usuarios WHERE nombre_usuario = 'empleado6';

-- Insertar usuarios base (RRHH)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario)
VALUES
    ('rrhh1', 'password', 'rrhh1@empresa.com', 'Elena Ruiz', 'RRHH', true, 'RRHH'),
    ('rrhh2', 'password', 'rrhh2@empresa.com', 'Pablo Moreno', 'RRHH', true, 'RRHH'),
    ('rrhh3', 'password', 'rrhh3@empresa.com', 'Carmen Jiménez', 'RRHH', true, 'RRHH');

-- Insertar RRHH (usando SELECT para obtener el ID del usuario recién insertado)
INSERT INTO rrhh (id, area_especializacion, certificaciones, fecha_incorporacion_rrhh)
SELECT id, 'Contratación', 'CIPD Level 5', '2022-05-12' FROM usuarios WHERE nombre_usuario = 'rrhh1'
UNION ALL
SELECT id, 'Formación', 'HR Management', '2021-08-15' FROM usuarios WHERE nombre_usuario = 'rrhh2'
UNION ALL
SELECT id, 'Selección', 'Talent Acquisition', '2022-11-10' FROM usuarios WHERE nombre_usuario = 'rrhh3';

-- Insertar proyectos
INSERT INTO proyectos (nombre, descripcion, jefe_id, fecha_inicio, fecha_fin_estimada, estado)
SELECT 'Sistema de Gestión', 'Sistema para gestión empresarial', id, '2023-01-20', '2023-12-31', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe1')
UNION ALL
SELECT 'App Móvil', 'Aplicación móvil para clientes', id, '2023-05-15', '2023-11-30', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe2')
UNION ALL
SELECT 'Portal Web Corporativo', 'Rediseño del portal web corporativo', id, '2023-03-01', '2023-09-30', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe3')
UNION ALL
SELECT 'Sistema de Análisis de Datos', 'Plataforma de análisis de datos para toma de decisiones', id, '2023-06-01', '2024-02-28', 'PLANIFICACION' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe4')
UNION ALL
SELECT 'Integración CRM', 'Integración del sistema CRM con otras plataformas', id, '2023-04-15', '2023-10-15', 'EN_PROGRESO' FROM jefes WHERE id = (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe5');

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

-- Nuevas tareas
INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad)
SELECT
    'Implementar autenticación',
    'Implementar sistema de autenticación OAuth2',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado4'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe3'),
    NOW() - INTERVAL 3 DAY,
    CURDATE() + INTERVAL 7 DAY,
    false,
    'ALTA';

INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad)
SELECT
    'Optimizar consultas SQL',
    'Revisar y optimizar consultas SQL para mejorar rendimiento',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado5'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe4'),
    NOW() - INTERVAL 2 DAY,
    CURDATE() + INTERVAL 4 DAY,
    false,
    'MEDIA';

INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad)
SELECT
    'Pruebas de integración',
    'Realizar pruebas de integración del módulo de pagos',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado6'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe5'),
    NOW() - INTERVAL 4 DAY,
    CURDATE() + INTERVAL 3 DAY,
    false,
    'ALTA';

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

-- Nuevas reuniones
INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id)
SELECT
    'Planificación sprint',
    'Planificación del próximo sprint',
    NOW() + INTERVAL 2 DAY + INTERVAL 9 HOUR + INTERVAL 30 MINUTE,
    'Sala C',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe3');

INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id)
SELECT
    'Revisión de diseño',
    'Revisión de los diseños de la interfaz',
    NOW() + INTERVAL 4 DAY + INTERVAL 15 HOUR,
    'Sala A',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe4');

INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id)
SELECT
    'Retrospectiva',
    'Retrospectiva del último sprint',
    NOW() + INTERVAL 1 DAY + INTERVAL 16 HOUR + INTERVAL 30 MINUTE,
    'Sala D',
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe5');

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

-- Participantes para las nuevas reuniones
INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Planificación sprint'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe3');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Planificación sprint'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado4');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Planificación sprint'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado5');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Revisión de diseño'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe4');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Revisión de diseño'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado2');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Revisión de diseño'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado6');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Retrospectiva'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'jefe5');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Retrospectiva'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado1');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Retrospectiva'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado3');

INSERT INTO reunion_participantes (reunion_id, usuario_id)
SELECT
    (SELECT id FROM reuniones WHERE titulo = 'Retrospectiva'),
    (SELECT id FROM usuarios WHERE nombre_usuario = 'empleado5');

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
