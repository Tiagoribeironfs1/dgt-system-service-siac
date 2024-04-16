package com.siac.controller;

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

    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse> uploadImages(@RequestParam("files") MultipartFile[] files,
                                                    @RequestParam("path") String path) {
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Pula arquivos vazios
                }
                imageStorageService.storeImage(file, path);
            }
            return ResponseEntity.ok(new ApiResponse(true, "Images uploaded successfully to: " + path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to upload images: " + e.getMessage()));
        }
    }
}
