package com.siac.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.siac.exception.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;


@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("image/jpeg", "image/png");

    public void storeImage(MultipartFile file, String storagePath) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
                throw new StorageException("Invalid file type. Only JPEG and PNG files are allowed.");
            }

            Path rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();

            // Check if the directory exists, if not throw an exception
            if (!Files.exists(rootLocation)) {
                throw new StorageException("Directory does not exist: " + rootLocation);
            }

            Path destinationFile = rootLocation.resolve(Paths.get(file.getOriginalFilename())).normalize();
            // Validate that the file is being stored within the designated directory
            if (!destinationFile.getParent().equals(rootLocation)) {
                throw new StorageException("Cannot store file outside the designated directory.");
            }

            file.transferTo(destinationFile);
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public boolean exists(String filename, String path) {
        Path imagePath = Paths.get(path, filename);
        return Files.exists(imagePath);
    }

    public void deleteImage(String filename, String path) {
        Path imagePath = Paths.get(path, filename);
        try {
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                System.out.println("Image deleted successfully: " + imagePath);
            } else {
                throw new StorageException("Image not found: " + imagePath);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete image: " + e.getMessage(), e);
        }
    }
    
}
