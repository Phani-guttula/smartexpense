package com.smartexpense.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartexpense.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {
    
    @Autowired
    private Cloudinary cloudinary;
    
    @Value("${upload.max-file-size:5242880}")  // 5MB default
    private long maxFileSize;
    
    @Value("${upload.allowed-extensions:jpg,jpeg,png,pdf}")
    private String allowedExtensions;
    
    //Upload receipt image to Cloudinary
    // Returns the secure URL of uploaded image
    public String uploadReceipt(MultipartFile file) throws IOException {
        
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;
        
        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "smartexpense/receipts",  // Organize in folder
                "public_id", filename,
                "resource_type", "auto",
                "overwrite", false,
                "quality", "auto:good"
            )
        );
        
        return (String) uploadResult.get("secure_url");
    }
   
    private void validateFile(MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }
      
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException(
                "File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB"
            );
        }
        
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);
        
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        if (!allowed.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                "Invalid file type. Allowed types: " + allowedExtensions
            );
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    public void deleteReceipt(String imageUrl) throws IOException {
        
   
        String publicId = extractPublicIdFromUrl(imageUrl);
        
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
    
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("smartexpense/receipts/")) {
            return null;
        }
        
        int startIndex = url.indexOf("smartexpense/receipts/");
        int endIndex = url.lastIndexOf(".");
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return url.substring(startIndex, endIndex);
        }
        
        return null;
    }
}