package com.rgcet.admission.service;

import com.rgcet.admission.common.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + root, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid file path.");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file: " + fileName, e);
        }
        return fileName;
    }

    public Path resolve(String fileName) {
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
        return path;
    }
}
