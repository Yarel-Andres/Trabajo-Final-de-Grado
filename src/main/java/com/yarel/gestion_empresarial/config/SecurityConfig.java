// Configuración de seguridad de Spring Security para manejo de  autenticación, autorización y protección CSRF

package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Optional;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Injeccion de repositorio de usuario para la autenticación personalizada
    //@Autowired
    //private UsuarioRepository usuarioRepository;


    // Autenticación personalizada: Permite login con nombre de usuario o nombre completo,
    // busca en la base de datos y valida credenciales
    @Bean
    public UserDetailsService userDetailsService() {
        // Autenticación temporal en memoria para testing
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder().encode("admin123"))
                        .roles("RRHH")
                        .build(),
                User.builder()
                        .username("jefe")
                        .password(passwordEncoder().encode("jefe123"))
                        .roles("JEFE")
                        .build(),
                User.builder()
                        .username("empleado")
                        .password(passwordEncoder().encode("empleado123"))
                        .roles("EMPLEADO")
                        .build()
        );
    }

    /*
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(username);

            if (usuario.isEmpty()) {
                usuario = usuarioRepository.findByNombreCompleto(username);
            }

            // Construye el objeto User de Spring Security con los datos del usuario: nombre completo,
            // contraseña encriptada con BCrypt y rol para su autorización
            if (usuario.isPresent()) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(usuario.get().getNombreCompleto())
                        .password(usuario.get().getContraseña())
                        .roles(usuario.get().getRol().name())
                        .build();
            } else {
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            }
        };
    }
    */


    // Codificacion  de contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Configuración principal de seguridad para reglas de autorización, autenticación y logout
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Protección mediante CSRF: Habilitada por defecto para prevenir ataques, está completamente habilitado

                // Autorización: Control de acceso basado en roles
                .authorizeHttpRequests(authz -> authz
                        // Página de login y recursos estáticos accesibles sin autenticación
                        .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()


                        // RRHH: Acceso completo a la aplicacióm
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/presupuestos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/proyectos/**")).hasRole("RRHH")

                        // JEFE: Funciones de gestión y supervisión
                        .requestMatchers(new AntPathRequestMatcher("/tareas/asignar")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/crear")).hasRole("JEFE")

                        // Acceso múltiple: Funciones compartidas para varios roles
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configuración para login: Login personalizado, Url que procesa credenciales, redireccionaminento al
                // al dashboard despues de un login con exito y acceso público al formulario para hacer login
                .formLogin(formLogin -> formLogin
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                // Configuración para logout: // Solo con POST por seguridad, redireccinamiento a la página de login
                // despues de logout con éxito, invalidacion de sesion, limpia la autenticación, elimina las cookies
                // de sesión y acceso público al boton de logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
