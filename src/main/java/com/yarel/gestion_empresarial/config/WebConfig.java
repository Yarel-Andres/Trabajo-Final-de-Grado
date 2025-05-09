// ** Configuración para el manejo de fechas en la aplicación

// Convierte automáticamente textos de fechas a objetos LocalDateTime. Facilita el procesamiento de fechas recibidas
// desde formularios web

package com.yarel.gestion_empresarial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }

    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            try {
                // Intentar parse estándar (formato ISO)
                return LocalDateTime.parse(source);
            } catch (Exception e1) {
                try {
                    // Intentar formato alternativo (dd/MM/yyyy HH:mm)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    return LocalDateTime.parse(source, formatter);
                } catch (Exception e2) {
                    // Registrar error y lanzar excepción comprensible
                    System.err.println("Error al convertir fecha: " + source);
                    e2.printStackTrace();
                    throw new IllegalArgumentException("Formato de fecha inválido: " + source);
                }
            }
        }
    }
}
