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
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/presupuestos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/rrhh/proyectos/**")).hasRole("RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/asignar")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/crear")).hasRole("JEFE")
                        .requestMatchers(new AntPathRequestMatcher("/tareas/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .requestMatchers(new AntPathRequestMatcher("/reuniones/**")).hasAnyRole("JEFE", "EMPLEADO", "RRHH")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                .requestMatchers(new AntPathRequestMatcher("/static/**"))
                .requestMatchers(new AntPathRequestMatcher("/css/**"))
                .requestMatchers(new AntPathRequestMatcher("/js/**"))
                .requestMatchers(new AntPathRequestMatcher("/scripts/**"))
                .requestMatchers(new AntPathRequestMatcher("/images/**"))
                .requestMatchers(new AntPathRequestMatcher("/webjars/**"))
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico"));
    }
}
