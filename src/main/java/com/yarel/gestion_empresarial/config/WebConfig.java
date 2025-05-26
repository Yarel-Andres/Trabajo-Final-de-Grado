// Configuración para el manejo de fechas en formularios web

package com.yarel.gestion_empresarial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Registra el convertidor personalizado de String a LocalDateTime para formularios
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    // Convertidor que transforma strings de fechas a objetos LocalDateTime
    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            try {
                // Formato estándar ISO (yyyy-MM-ddTHH:mm)
                return LocalDateTime.parse(source);
            } catch (Exception e1) {
                try {
                    // Formato alternativo para formularios HTML (yyyy-MM-dd HH:mm)
                    if (source.contains(" ")) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        return LocalDateTime.parse(source, formatter);
                    }

                    // Si no se puede convertir, lanza excepción de error
                    throw new IllegalArgumentException("Formato de fecha inválido: " + source);
                } catch (Exception e2) {
                    System.err.println("Error al convertir fecha: " + source);
                    throw new IllegalArgumentException("Formato de fecha inválido: " + source);
                }
            }
        }
    }
}
