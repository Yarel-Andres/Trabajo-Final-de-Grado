// Componente de migración de contraseñas

// Se ejecuta automáticamente al iniciar la aplicación y convierte
// todas las contraseñas en texto plano a contraseñas encriptadas con BCrypt.
package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component // - COMENTADO TEMPORALMENTE PARA TESTING
//@ConditionalOnProperty(name = "app.password-updater.enabled", havingValue = "true")
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

        // Contador para saber cuántas contraseñas se actualizaron
        int actualizados = 0;

        // Itera sobre cada usuario para verificar su contraseña
        for (Usuario usuario : usuarios) {
            if (!usuario.getContraseña().startsWith("$2a$")) {
                // Guarda la contraseña original antes de encriptarla
                String originalPassword = usuario.getContraseña();
                usuario.setContraseña(passwordEncoder.encode(originalPassword));

                // Guarda el usuario con la contraseña encriptada en la base de datos
                usuarioRepository.save(usuario);

                actualizados++;

                // Registra qué usuario fue actualizado
                log.info("Actualizada contraseña para usuario: {}", usuario.getNombreUsuario());
            }
            // Si la contraseña ya empieza con $2a$, no hace nada
        }

        // Registra el resultado final del proceso
        log.info("Proceso completado. {} contraseñas actualizadas.", actualizados);
    }
}
