package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size must not exceed 5MB");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new FileStorageException("Only PDF, JPG, and PNG files are allowed");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath);

            return uniqueFileName;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }
}