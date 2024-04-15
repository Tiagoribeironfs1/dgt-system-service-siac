package com.siac.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.siac.exception.StorageException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Service
public class ImageStorageService {

    private final Path rootLocation = Paths.get("/mnt/share/sistemas/ImagensPecas");
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("image/jpeg", "image/png");

    public void storeImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
                throw new StorageException("Invalid file type. Only JPEG and PNG files are allowed.");
            }

            Path destinationFile = this.rootLocation.resolve(
                Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside current directory.");
            }
            file.transferTo(destinationFile);
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }
}
