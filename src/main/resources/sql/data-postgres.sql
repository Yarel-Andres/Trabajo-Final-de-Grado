-- Orden de Usuarios= Roles, Proyectos, Tareas, Reuniones, Registros


-- 1. USUARIOS

-- Jefes (Ids 1, 2, 3, 4 y 5)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario) VALUES
    ('jefe1', 'pass', 'jefe1@empresa.com', 'Yarel', 'JEFE', true, 'JEFE'),
    ('jefe2', 'pass', 'jefe2@empresa.com', 'Amado', 'JEFE', true, 'JEFE'),
    ('jefe3', 'pass', 'jefe3@empresa.com', 'Moises', 'JEFE', true, 'JEFE'),
    ('jefe4', 'pass', 'jefe4@empresa.com', 'Zacarias', 'JEFE', true, 'JEFE'),
    ('jefe5', 'pass', 'jefe5@empresa.com', 'Efren', 'JEFE', true, 'JEFE');


-- Empleados (Ids 6, 7, 8, 9, 10 y 11)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario) VALUES
('empleado1', 'pass', 'empleado1@empresa.com', 'Edson', 'EMPLEADO', true, 'EMPLEADO'),
('empleado2', 'pass', 'empleado2@empresa.com', 'Jose', 'EMPLEADO', true, 'EMPLEADO'),
('empleado3', 'pass', 'empleado3@empresa.com', 'Ramiro', 'EMPLEADO', true, 'EMPLEADO'),
('empleado4', 'pass', 'empleado4@empresa.com', 'Raul', 'EMPLEADO', true, 'EMPLEADO'),
('empleado5', 'pass', 'empleado5@empresa.com', 'Erick', 'EMPLEADO', true, 'EMPLEADO'),
('empleado6', 'pass', 'empleado6@empresa.com', 'Jaime', 'EMPLEADO', true, 'EMPLEADO');


-- RRHH (Ids 12, 13, 14)
INSERT INTO usuarios (nombre_usuario, contraseña, correo, nombre_completo, rol, activo, tipo_usuario) VALUES
    ('rrhh1', 'pass', 'rrhh1@empresa.com', 'Elena', 'RRHH', true, 'RRHH'),
    ('rrhh2', 'pass', 'rrhh2@empresa.com', 'Ana', 'RRHH', true, 'RRHH'),
    ('rrhh3', 'pass', 'rrhh3@empresa.com', 'Sara', 'RRHH', true, 'RRHH');



-- 2. ROLES Concretos (utilizando los Ids generados secuencialmente)

-- Jefes (Ids 1 a 5)
INSERT INTO jefes (id, fecha_nombramiento, nivel_responsabilidad) VALUES
    (1, '2022-01-15', 'Senior'),
    (2, '2023-03-10', 'Medio'),
    (3, '2021-05-20', 'Senior'),
    (4, '2022-08-15', 'Junior'),
    (5, '2023-01-05', 'Medio');


-- Empleados (Ids 6 a 11)
INSERT INTO empleados (id, fecha_contratacion, puesto, telefono, salario) VALUES
    (6, '2023-02-20', 'Desarrollador', '666111222', 32000.0),
    (7, '2022-11-05', 'Diseñador UX', '666333444', 29000.0),
    (8, '2023-06-15', 'Analista', '666555666', 27500.0),
    (9, '2023-03-10', 'Desarrollador Frontend', '666777888', 31000.0),
    (10, '2022-09-01', 'Desarrollador Backend', '666999000', 33000.0),
    (11, '2023-04-05', 'QA Tester', '666123456', 28500.0);


-- RRHH (IDs 12 a 14)
INSERT INTO rrhh (id, area_especializacion, certificaciones, fecha_incorporacion_rrhh) VALUES
    (12, 'Contratación', 'CIPD Level 5', '2022-05-12'),
    (13, 'Formación', 'HR Management', '2021-08-15'),
    (14, 'Selección', 'Talent Acquisition', '2022-11-10');



-- 3. PROYECTOS
INSERT INTO proyectos (nombre, descripcion, jefe_id, fecha_inicio, fecha_fin_estimada, estado, completado) VALUES
    ('Sistema de Gestión', 'Sistema para gestión empresarial', 1, '2023-01-20', '2023-12-31', 'EN_PROGRESO', false),
    ('App Móvil', 'Aplicación móvil para clientes', 2, '2023-05-15', '2023-11-30', 'EN_PROGRESO', false),
    ('Portal Web Corporativo', 'Rediseño del portal web corporativo', 3, '2023-03-01', '2023-09-30', 'EN_PROGRESO', false),
    ('Integración CRM', 'Integración del sistema CRM con otras plataformas', 5, '2023-04-15', '2023-10-15', 'EN_PROGRESO', false),
    ('Proyecto Colaborativo', 'Proyecto para demostrar la colaboración entre equipos', 2, '2023-04-10', '2023-12-15', 'EN_PROGRESO', false);

-- Asignación de empleados a proyectos
INSERT INTO proyecto_empleados (proyecto_id, empleado_id) VALUES
    (5, 6), -- Proyecto Colaborativo con Edson
    (5, 7), -- Proyecto Colaborativo con Jose
    (5, 8), -- Proyecto Colaborativo con Ramiro
    (5, 9); -- Proyecto Colaborativo con Raul



-- 5. TAREAS
INSERT INTO tareas (titulo, descripcion, empleado_id, jefe_id, fecha_creacion, fecha_vencimiento, completada, prioridad, proyecto_id) VALUES
    -- Tareas de App Móvil (Proyecto ID 2, Jefe ID 2)
    ('Análisis de requisitos', 'Analizar requisitos del cliente', 8, 2, NOW() - INTERVAL '10 days', CURRENT_DATE + INTERVAL '7 days', false, 'BAJA', 2),

    -- Tareas del Portal Web (Proyecto ID 3, Jefe ID 3)
    ('Implementar autenticación', 'Implementar sistema de autenticación OAuth2', 9, 3, NOW() - INTERVAL '3 days', CURRENT_DATE + INTERVAL '7 days', false, 'ALTA', 3),

    -- Tareas de Integración CRM (Proyecto ID 4, Jefe ID 5)
    ('Pruebas de integración', 'Realizar pruebas de integración del módulo de pagos', 11, 5, NOW() - INTERVAL '4 days', CURRENT_DATE + INTERVAL '3 days', false, 'ALTA', 4),

    -- Tarea adicional para Sistema de Gestión (Proyecto ID 1, Jefe ID 1)
    ('Optimizar consultas SQL', 'Revisar y optimizar consultas SQL para mejorar rendimiento', 10, 1, NOW() - INTERVAL '2 days', CURRENT_DATE + INTERVAL '4 days', false, 'MEDIA', 1);


-- 6. REUNIONES
INSERT INTO reuniones (titulo, descripcion, fecha_hora, sala, organizador_id) VALUES
('Kickoff proyecto', 'Reunión inicial del proyecto', NOW() + INTERVAL '3 days' + INTERVAL '10 hours', 'Sala A', 1),
('Revisión de avances', 'Revisión semanal de avances', NOW() + INTERVAL '7 days' + INTERVAL '11 hours', 'Sala B', 2),
('Planificación sprint', 'Planificación del próximo sprint', NOW() + INTERVAL '2 days' + INTERVAL '9 hours' + INTERVAL '30 minutes', 'Sala C', 3),
('Revisión de diseño', 'Revisión de los diseños de la interfaz', NOW() + INTERVAL '4 days' + INTERVAL '15 hours', 'Sala A', 4),
('Retrospectiva', 'Retrospectiva del último sprint', NOW() + INTERVAL '1 day' + INTERVAL '16 hours' + INTERVAL '30 minutes', 'Sala D', 5);


-- Participantes de reuniones

-- Kickoff proyecto
INSERT INTO reunion_participantes (reunion_id, usuario_id) VALUES
(1, 8),  -- Yarel (Jefe)
(1, 6),  -- Edson (Empleado)
(1, 7);  -- Jose (Empleado)

-- Revisión de avances (Reunion ID 2)
INSERT INTO reunion_participantes (reunion_id, usuario_id) VALUES
(2, 2),  -- Amado (Jefe)
(2, 8);  -- Ramiro (Empleado)

-- Planificación sprint (Reunion ID 3)
INSERT INTO reunion_participantes (reunion_id, usuario_id) VALUES
(3, 3),  -- Moises (Jefe)
(3, 9),  -- Raul (Empleado)
(3, 10); -- Erick (Empleado)

-- Revisión de diseño (Reunion ID 4)
INSERT INTO reunion_participantes (reunion_id, usuario_id) VALUES
(4, 4),  -- Zacarias (Jefe)
(4, 7),  -- Jose (Empleado)
(4, 11); -- Jaime (Empleado)

-- Retrospectiva (Reunion ID 5)
INSERT INTO reunion_participantes (reunion_id, usuario_id) VALUES
(5, 5),  -- Efren (Jefe)
(5, 6),  -- Edson (Empleado)
(5, 8),  -- Ramiro (Empleado)
(5, 10); -- Erick (Empleado)


-- 8. REGISTROS DE TIEMPO (Asumiendo que las tareas tienen IDs 1-6)
INSERT INTO registros_tiempo (usuario_id, tarea_id, proyecto_id, reunion_id, fecha_registro, hora_inicio, hora_fin, horas_trabajadas, comentario) VALUES
    (6, 1, 4, NULL, CURRENT_DATE - INTERVAL '2 days', NOW() - INTERVAL '26 hours', NOW() - INTERVAL '22 hours', 4.0, 'Avance en la implementación de la API'),
    (7, 2, 2, NULL, CURRENT_DATE - INTERVAL '1 day', NOW() - INTERVAL '25 hours', NOW() - INTERVAL '21 hours', 4.0, 'Diseño de la interfaz principal'),
    (8, 3, 2, NULL, CURRENT_DATE - INTERVAL '3 days', NOW() - INTERVAL '30 hours', NOW() - INTERVAL '26 hours', 4.0, 'Análisis de requisitos completado'),
    (9, 4, 3, NULL, CURRENT_DATE - INTERVAL '1 day', NOW() - INTERVAL '24 hours', NOW() - INTERVAL '20 hours', 4.0, 'Implementación de OAuth2');




