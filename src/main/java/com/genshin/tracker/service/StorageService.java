package com.genshin.tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService(@Value("${app.storage.receipts-dir:./data/receipts}") String receiptsDir) {
        this.rootLocation = Paths.get(receiptsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        String storedFileName = UUID.randomUUID().toString() + extension;
        Path destination = this.rootLocation.resolve(storedFileName).normalize();

        // Path traversal protection
        if (!destination.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside target directory.");
        }

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return storedFileName;
    }

    public Resource loadAsResource(String storedFileName) {
        try {
            Path file = this.rootLocation.resolve(storedFileName).normalize();
            if (!file.getParent().equals(this.rootLocation)) {
                throw new SecurityException("Cannot access file outside target directory.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + storedFileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + storedFileName, e);
        }
    }

    public Path getFilePath(String storedFileName) {
        return this.rootLocation.resolve(storedFileName).normalize();
    }

    public boolean delete(String storedFileName) {
        try {
            Path file = this.rootLocation.resolve(storedFileName).normalize();
            if (file.getParent().equals(this.rootLocation)) {
                return Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {}
        return false;
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
