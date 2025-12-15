package com.example.learning_service.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            // Create main upload directory
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created main upload directory: {}", uploadPath.toAbsolutePath());
            }
            
            // Create subdirectories for different file types
            String[] subDirectories = {
                "courses/thumbnails",
                "courses/previews",
                "lessons/videos",
                "lessons/documents",
                "blogs/images",
                "certificates",
                "profiles"
            };
            
            for (String subDir : subDirectories) {
                Path subPath = uploadPath.resolve(subDir);
                if (!Files.exists(subPath)) {
                    Files.createDirectories(subPath);
                    log.info("Created subdirectory: {}", subPath.toAbsolutePath());
                }
            }
            
            log.info("File storage initialization completed successfully");
        } catch (IOException e) {
            log.error("Failed to initialize upload directories", e);
            throw new RuntimeException("Could not create upload directories!", e);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        log.info("Storing file - Original name: {}, Size: {}, ContentType: {}", 
            fileName, file.getSize(), file.getContentType());
        
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        log.info("Generated new filename: {}", newFileName);

        Path uploadPath = Paths.get(uploadDir, subDirectory);
        log.info("Upload directory: {}", uploadDir);
        log.info("Sub directory: {}", subDirectory);
        log.info("Full upload path: {}", uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        Path targetLocation = uploadPath.resolve(newFileName);
        log.info("Target location: {}", targetLocation.toAbsolutePath());
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("File copied successfully to: {}", targetLocation.toAbsolutePath());

        String returnPath = subDirectory + "/" + newFileName;
        log.info("Returning path: {}", returnPath);
        
        return returnPath;
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        log.info("Attempting to delete file - Full path: {}", path.toAbsolutePath());
        log.info("File exists: {}", Files.exists(path));
        
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted successfully: {}", path.toAbsolutePath());
        } else {
            log.warn("File not found for deletion: {}", path.toAbsolutePath());
            Files.deleteIfExists(path); // Won't throw if doesn't exist
        }
    }

    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("video/");
    }
}
