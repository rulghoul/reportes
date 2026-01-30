package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.utils.Utilidades;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class Images {

    private final ResourceLoader resourceLoader;

    @Autowired
    public Images(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public File recuperaModelo(String marca, String modelo) throws IOException {
        try {
            String imagePath = "static/images/modelos/" + Utilidades.sanitazeName(modelo).toLowerCase() + ".png";
            return getResourceAsFile(imagePath);
        } catch (Exception e) {
            log.error("Error al recuperar imagen del modelo: {}", modelo, e);
            return getResourceAsFile("static/errorImage.png");
        }
    }

    public File recuperaMarca(String marca) throws IOException {
        try {
            String imagePath = "static/images/marcas/" + Utilidades.sanitazeName(marca).toLowerCase() + ".png";
            return getResourceAsFile(imagePath);
        } catch (Exception e) {
            log.error("Error al recuperar imagen de la marca: {}", marca, e);
            return getResourceAsFile("static/errorImage.png");
        }
    }

    private File getResourceAsFile(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);

        if (!resource.exists()) {
            throw new IOException("Recurso no encontrado: " + resourcePath);
        }

        // Si el recurso está en el sistema de archivos (desarrollo), usar getFile()
        if (resource.getURI().getScheme().equals("file")) {
            return resource.getFile();
        }

        // Si el recurso está dentro del JAR, crear un archivo temporal
        try (InputStream inputStream = resource.getInputStream()) {
            Path tempFile = Files.createTempFile("resource-", ".tmp");
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFile.toFile();
        }
    }
}