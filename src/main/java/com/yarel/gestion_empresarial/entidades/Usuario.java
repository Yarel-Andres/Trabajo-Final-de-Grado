package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

// Indica que esta clase es una entidad JPA que se mapea con la base de datos
@Entity

// Especifica que el nombre de la clase en la base de datos se llama usuarios
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor

// Excluye las colecciones del hashCode y equals para evitar ciclos infinitos
@EqualsAndHashCode(exclude = {"tareas", "proyectos", "reuniones", "registrosTiempo"})

// Define una estrategia de herencia. Esto significa que si otras clases extienden esta clase base (Usuario), las
// tablas estarán unidas mediante claves foraneas
@Inheritance(strategy = InheritanceType.JOINED)

// Establece una columna discriminadora para diferenciar los tipos de usuarios cuando hay herencia
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Debe ser unico y no puede ser nulo
    // El nombre de usuario es distinto que el nombre completo
    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    @Column(nullable = false)
    private String contraseña;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    // Especifica que el rol se almacenará como un texto en la base de datos en lugar de como un número entero
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolEnum rol;

    @Column
    private boolean activo = true;

    // Enum para los roles
    public enum RolEnum {
        RRHH, JEFE, EMPLEADO
    }
}
