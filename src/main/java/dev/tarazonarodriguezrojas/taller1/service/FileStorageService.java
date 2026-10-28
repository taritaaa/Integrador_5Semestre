package dev.tarazonarodriguezrojas.taller1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Se encarga de guardar y borrar archivos en el disco.
 * La carpeta se configura en application.properties con app.upload-dir.
 */
@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload-dir:uploads/parts}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de imágenes: " + rootLocation, e);
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }

    /**
     * Guarda el archivo con un nombre generado (UUID + extensión) y devuelve ese nombre.
     * No se usa el nombre original para evitar nombres repetidos o peligrosos.
     */
    public String store(MultipartFile file, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo", e);
        }
        return fileName;
    }

    // Borra un archivo guardado. Si no existe o no se puede borrar, no se interrumpe la operación.
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        Path target = rootLocation.resolve(fileName).normalize();
        // Seguridad: solo se borra dentro de la carpeta de imágenes
        if (!target.startsWith(rootLocation)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Un archivo que no se pudo borrar no debe impedir la operación principal
        }
    }
}