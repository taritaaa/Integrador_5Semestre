package dev.tarazonarodriguezrojas.taller1.config;

import dev.tarazonarodriguezrojas.taller1.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Publica la carpeta de imágenes de repuestos para que el navegador pueda mostrarlas.
 * Una imagen guardada como "abc.png" se ve en /part-images/abc.png
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorage;

    public WebConfig(FileStorageService fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorage.getRootLocation().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/part-images/**").addResourceLocations(location);
    }
}