package com.siac.controller;

import com.siac.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImagesController {

    private static final Logger log = LoggerFactory.getLogger(ImagesController.class);

    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse> uploadImages(@RequestParam("files") MultipartFile[] files,
                                                    @RequestParam("path") String path) {
        try {
            int uploadCount = 0;
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Skip empty files
                }
                imageStorageService.storeImage(file, path);
                uploadCount++;
            }
            log.info("Uploaded {} images successfully to {}", uploadCount, path);
            return ResponseEntity.ok(new ApiResponse(true, "Images uploaded successfully. Total: " + uploadCount));
        } catch (Exception e) {
            log.error("Failed to upload images to {}: {}", path, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to upload images: " + e.getMessage()));
        }
    }
}
