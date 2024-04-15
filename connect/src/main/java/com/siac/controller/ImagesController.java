package com.siac.controller;

import com.siac.exception.StorageException;
import com.siac.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImagesController {

    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            imageStorageService.storeImage(file);
            return ResponseEntity.ok(new ApiResponse(true, "Image uploaded successfully"));
        } catch (StorageException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to upload image: " + ex.getMessage()));
        }
    }
}
