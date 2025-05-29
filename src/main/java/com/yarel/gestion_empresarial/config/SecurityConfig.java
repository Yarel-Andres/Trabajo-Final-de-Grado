package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(username);
            if (usuario.isEmpty()) {
                usuario = usuarioRepository.findByNombreCompleto(username);
            }
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(new AntPathRequestMatcher("/")).permitAll() // Permitir acceso a la página de login
                        // Aquí van tus reglas de autorización específicas para roles
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/presupuestos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/proyectos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/asignar")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/crear")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .anyRequest().authenticated() // Todas las demás peticiones requieren autenticación
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/") // Especifica tu página de login personalizada
                        .loginProcessingUrl("/login") // URL a la que se envían las credenciales
                        .defaultSuccessUrl("/dashboard", true) // Página a la que se redirige tras login exitoso
                        .permitAll() // Permitir acceso a la página de login a todos
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // URL para logout, preferiblemente POST
                        .logoutSuccessUrl("/") // Página a la que se redirige tras logout exitoso
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        // CSRF está habilitado por defecto, lo cual es bueno.
        // Si necesitas deshabilitarlo (no recomendado para producción): http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Configura Spring Security para ignorar las rutas a los recursos estáticos.
        // Esto es más eficiente que pasar por toda la cadena de filtros.
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/static/**")) // Carpeta 'static' general
                .requestMatchers(new AntPathRequestMatcher("/css/**"))   // Para tus archivos CSS
                .requestMatchers(new AntPathRequestMatcher("/js/**"))    // Para archivos JavaScript (si los pones en /static/js/)
                .requestMatchers(new AntPathRequestMatcher("/scripts/**")) // Para tus archivos JavaScript en /static/scripts/
                .requestMatchers(new AntPathRequestMatcher("/images/**")) // Para imágenes
                .requestMatchers(new AntPathRequestMatcher("/webjars/**")) // Para WebJars como Bootstrap, FontAwesome
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico"));
    }
}
