// Configuracion de la seguridad para que jefe, empleado y rrhh se logeen y puedan realizar las
// funcionalidades que se le permiten segun su rol. Que un jefe asigne una tarea a un empleado, etc...
package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.entidades.Usuario.RolEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Configuration
// Habilita la seguridad en la aplicación, activando las características de Spring Security
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    // Se busca el usuario en usuarioRepository y, si existe, se transforma en un usuario de Spring Security
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(username);
            // Si el usuario se encuentra, se crea una instancia de User con su nombre de usuario, contraseña yrol
            if (usuario.isPresent()) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(usuario.get().getNombreUsuario())
                        .password(usuario.get().getContraseña())
                        .roles(usuario.get().getRol().name())
                        .build();
            } else {
                //Si el usuario no existe, se lanza una excepción UsernameNotFoundException
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usar NoOpPasswordEncoder para desarrollo (no encripta las contraseñas)
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }


    @Bean
    // Este metodo configura cómo se manejan las solicitudes HTTP en términos de seguridad.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF está deshabilitado. Esto facilita pruebas en desarrollo, pero debe ser habilitado en producción
                .csrf(csrf -> csrf.disable())
                // Aquí se establecen reglas de acceso a diferentes endpoints
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso a recursos estáticos y página de inicio
                        .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/registro/**")).permitAll()

                        // Permitir acceso a h2-console solo a administradores (para desarrollo)
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).hasRole("ADMIN")


                        // Rutas para tareas
                        .requestMatchers(new AntPathRequestMatcher("/tareas/asignar")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/ver")).hasRole("EMPLEADO")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")



                        /* Comentamos de momento los accesos a tareas, estos son los que funcionan para las pruebas en
                            el back con postman
                        // Restringir acceso a tareas según el rol
                        .requestMatchers(new AntPathRequestMatcher("/tareas/jefe")).hasRole("JEFE") // Jefe crea tareas
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH") //Acceso a todos

                        // Permitir a los empleados ver sus propias tareas, asi como a jefes y rrhh
                        .requestMatchers(new AntPathRequestMatcher("/api/tareas/empleado/**")).hasAnyRole("EMPLEADO", "JEFE", "RRHH")
                        */


                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/")  // Usar nuestra página de login personalizada
                        .loginProcessingUrl("/login")  // URL para procesar el login
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // Configuración  de seguridad de los encabezados HTTP
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );
                 return http.build();
    }
}
