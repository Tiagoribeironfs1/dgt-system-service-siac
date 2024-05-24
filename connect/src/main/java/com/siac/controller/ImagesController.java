package com.siac.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.siac.exception.StorageException;
import com.siac.service.ImageStorageService;

@RestController
public class ImagesController {


    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse> uploadImages(@RequestParam("files") MultipartFile[] files,
                                                    @RequestParam("path") String path) {
        Logger log = LoggerFactory.getLogger(ImagesController.class);
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
    
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Images uploaded successfully. Total: " + uploadCount);
            return ResponseEntity.ok(new ApiResponse(true, data));
        } catch (Exception e) {
            log.error("Failed to upload images to {}: {}", path, e.getMessage(), e);
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, errorData));
        }
    }
    
    
    @PostMapping("/product-images/{productCode}")
    public ResponseEntity<ApiResponse> checkProductImages(@PathVariable String productCode, @RequestBody Map<String, String> requestBody) {
        String baseUrl = requestBody.get("baseUrl");
        String basePath = requestBody.get("basePath"); 

        List<String> images = new ArrayList<>();
        String[] suffixes = {"", "A", "B", "C", "D", "E"}; // Suffixes for image variants
    
        for (String suffix : suffixes) {
            String imageName = productCode + suffix + ".jpg";
            if (imageStorageService.exists(imageName, basePath)) {
                images.add(baseUrl + imageName);
            }
        }
    
        if (images.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(false, new HashMap<String, Object>()));
        } else {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productImages", images);
            return ResponseEntity.ok(new ApiResponse(true, responseData));
        }
    }

    @DeleteMapping("/delete-images")
    public ResponseEntity<ApiResponse> deleteImages(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseData = new HashMap<>();
        boolean allDeleted = true;
    
        String basePath = (String) request.get("basePath");
        
        @SuppressWarnings("unchecked")
        List<String> filenames = (List<String>) request.get("filenames");
    
        for (String filename : filenames) {
            try {
                imageStorageService.deleteImage(filename, basePath);
            } catch (StorageException e) {
                allDeleted = false;
                responseData.put("message", "Error deleting one or more images");
                responseData.put("errors", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, responseData));
            }
        }
    
        responseData.put("message", "Images deleted successfully");
        return ResponseEntity.ok(new ApiResponse(allDeleted, responseData));
    }
}
