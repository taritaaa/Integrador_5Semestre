package dev.tarazonarodriguezrojas.taller1.config;

import dev.tarazonarodriguezrojas.taller1.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web:
 * - Publica la carpeta de imágenes de repuestos para que el navegador pueda mostrarlas.
 *   Una imagen guardada como "abc.png" se ve en /part-images/abc.png
 * - Registra el interceptor que obliga a cambiar la contraseña temporal.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorage;
    private final PasswordChangeInterceptor passwordChangeInterceptor;

    public WebConfig(FileStorageService fileStorage, PasswordChangeInterceptor passwordChangeInterceptor) {
        this.fileStorage = fileStorage;
        this.passwordChangeInterceptor = passwordChangeInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorage.getRootLocation().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/part-images/**").addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Se excluyen los archivos estáticos y las páginas de error para no bloquear el estilo ni los mensajes
        registry.addInterceptor(passwordChangeInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/part-images/**", "/error", "/access-denied");
    }
}
