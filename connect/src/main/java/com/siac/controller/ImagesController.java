package com.siac.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger log = LoggerFactory.getLogger(ImagesController.class);

    private static final String[] IMAGE_SUFFIXES = {"", "A", "B", "C", "D", "E"};
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "svg", "webp"};

    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse> uploadImages(@RequestParam("files") MultipartFile[] files,
                                                    @RequestParam("path") String path) {
        if (path == null || path.isEmpty()) {
            return createErrorResponse("Invalid path");
        }

        try {
            int uploadCount = storeImages(files, path);
            log.info("Uploaded {} images successfully to {}", uploadCount, path);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "Images uploaded successfully. Total: " + uploadCount);
            return ResponseEntity.ok(new ApiResponse(true, data));
        } catch (Exception e) {
            log.error("Failed to upload images to {}: {}", path, e.getMessage(), e);
            return createErrorResponse("Failed to upload images: " + e.getMessage());
        }
    }

    private int storeImages(MultipartFile[] files, String path) {
        int uploadCount = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                imageStorageService.storeImage(file, path);
                uploadCount++;
            }
        }
        return uploadCount;
    }

    @PostMapping("/product-images/{productCode}")
    public ResponseEntity<ApiResponse> checkProductImages(@PathVariable String productCode,
                                                          @RequestBody Map<String, String> requestBody) {
        String baseUrl = requestBody.get("baseUrl");
        String basePath = requestBody.get("basePath");

        if (productCode == null || productCode.isEmpty() || basePath == null || baseUrl == null) {
            return createErrorResponse("Invalid product code or base path");
        }

        List<String> images = findProductImages(productCode, basePath, baseUrl);

        if (images.isEmpty()) {
            return createErrorResponse("No images found for product: " + productCode);
        } else {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productImages", images);
            return ResponseEntity.ok(new ApiResponse(true, responseData));
        }
    }

    private List<String> findProductImages(String productCode, String basePath, String baseUrl) {
        List<String> images = new ArrayList<>();
        for (String suffix : IMAGE_SUFFIXES) {
            for (String extension : IMAGE_EXTENSIONS) {
                String imageName = productCode + suffix + "." + extension;
                if (imageStorageService.exists(imageName, basePath)) {
                    images.add(baseUrl + imageName);
                    break; // Stop checking other extensions once an image is found
                }
            }
        }
        return images;
    }

    @DeleteMapping("/delete-images")
    public ResponseEntity<ApiResponse> deleteImages(@RequestBody Map<String, Object> request) {
        List<String> filenames = extractFilenames(request);
        if (filenames == null || filenames.isEmpty()) {
            return createErrorResponse("No filenames provided");
        }

        String basePath = (String) request.get("basePath");

        try {
            deleteImagesFromStorage(filenames, basePath);
            return createSuccessResponse("Images deleted successfully");
        } catch (StorageException e) {
            log.error("Error deleting images: {}", e.getMessage(), e);
            return createErrorResponse("Error deleting images: " + e.getMessage());
        }
    }

    private List<String> extractFilenames(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> filenames = (List<String>) request.get("filenames");
        return filenames;
    }

    private void deleteImagesFromStorage(List<String> filenames, String basePath) {
        for (String filename : filenames) {
            imageStorageService.deleteImage(filename, basePath);
        }
    }

    private ResponseEntity<ApiResponse> createErrorResponse(String message) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", message);
        return ResponseEntity.badRequest().body(new ApiResponse(false, errorData));
    }

    private ResponseEntity<ApiResponse> createSuccessResponse(String message) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", message);
        return ResponseEntity.ok(new ApiResponse(true, responseData));
    }
}
