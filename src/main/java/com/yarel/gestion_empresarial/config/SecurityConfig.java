package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.entidades.Usuario;
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

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            // Primero intentamos buscar por nombre de usuario
            Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(username);

            // Si no encontramos por nombre de usuario, intentamos por nombre completo
            if (usuario.isEmpty()) {
                usuario = usuarioRepository.findByNombreCompleto(username);
            }

            // Si el usuario se encuentra, se crea una instancia de User con su nombre de usuario, contraseña y rol
            if (usuario.isPresent()) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(usuario.get().getNombreCompleto()) // Usamos nombre completo como username
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
        // Usar BCryptPasswordEncoder para cifrar contraseñas
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Habilitar CSRF (está habilitado por defecto, pero lo dejamos explícito)
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                // Aquí se establecen reglas de acceso a diferentes endpoints
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso a recursos estáticos y página de inicio
                        .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/registro/**")).permitAll()

                        // Permitir acceso a h2-console solo a administradores (para desarrollo)
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).hasRole("ADMIN")

                        // Rutas para RRHH
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/presupuestos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/proyectos/**")).hasRole("RRHH")

                        // Rutas para tareas
                        .requestMatchers(new AntPathRequestMatcher("/tareas/asignar")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/ver")).hasRole("EMPLEADO")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")

                        // Rutas para reuniones
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/crear")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/ver")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")

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
                        // Modificación principal: especificar el método POST para logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Configuración de seguridad de los encabezados HTTP
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );
        return http.build();
    }
}