package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PasswordUpdater implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordUpdater.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordUpdater(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando contraseñas de usuarios...");
        List<Usuario> usuarios = usuarioRepository.findAll();
        int actualizados = 0;

        for (Usuario usuario : usuarios) {
            // Solo actualiza si la contraseña no parece estar encriptada
            if (!usuario.getContraseña().startsWith("$2a$")) {
                String originalPassword = usuario.getContraseña();
                usuario.setContraseña(passwordEncoder.encode(originalPassword));
                usuarioRepository.save(usuario);
                actualizados++;
                log.info("Actualizada contraseña para usuario: {}", usuario.getNombreUsuario());
            }
        }

        log.info("Proceso completado. {} contraseñas actualizadas.", actualizados);
    }
}