# Despliegue del Proyecto Gestify

## Puesta en Marcha del Proyecto en Internet

Para que nuestra aplicación Gestify funcione online, seguimos estos pasos principales:

### 1. Nueva Base de Datos
Elegimos y configuramos una base de datos **PostgreSQL** en la plataforma **Render**, que es donde alojamos el proyecto.

### 2. Adaptación de Instrucciones
Modificamos los archivos que definen la estructura y los datos iniciales de la base de datos para que fueran compatibles con PostgreSQL.

### 3. Conexión del Programa
Ajustamos la configuración principal del programa para que pudiera comunicarse correctamente con la nueva base de datos en Render y cargar los datos al iniciar.

### 4. Ajustes de Seguridad
Solucionamos un problema que impedía que la página se viera bien, permitiendo que se cargaran los estilos y otros archivos visuales.

### 5. Corrección de Diseño
Arreglamos un detalle para que las plantillas de diseño y los iconos (Bootstrap y FontAwesome) se mostraran correctamente.

### 6. Solución de Errores
Corregimos algunos errores técnicos que surgieron, como problemas con archivos de configuración o incompatibilidades menores.

### 7. Despliegue Final
Usamos archivos de configuración (`Dockerfile`, `render.yaml`) y subimos los cambios a **GitHub** para que Render publicara la aplicación online, repitiendo hasta que todo funcionó correctamente.

---

**Resultado:** La aplicación está ahora disponible y funcionando en internet a través de la plataforma Render.

## Tecnologías Utilizadas
- **Backend:** Spring Boot + PostgreSQL
- **Frontend:** Thymeleaf + Bootstrap + FontAwesome
- **Despliegue:** Render + Docker
- **Control de Versiones:** GitHub