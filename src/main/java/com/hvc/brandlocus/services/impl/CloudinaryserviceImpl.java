package com.hvc.brandlocus.services.impl;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hvc.brandlocus.dto.response.UploadImageResponse;
import com.hvc.brandlocus.services.CloudinaryService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;


@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryserviceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;



    @Override
    public ResponseEntity<ApiResponse<?>> uploadFile(MultipartFile multipartFile) {
        try {

            // Check file size before processing
            long fileSizeInMB = multipartFile.getSize() / (1024 * 1024);
            log.info("File size: {} MB", fileSizeInMB);

            if (fileSizeInMB > 10) { // Cloudinary free tier limit
                log.warn("File size exceeds Cloudinary free tier limit: {} MB", fileSizeInMB);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("file_too_large",
                                "File size (" + fileSizeInMB + "MB) exceeds the 10MB limit for free tier"));
            }

            String fileUrl = null;
            String contentType = multipartFile.getContentType();
            String originalFilename = multipartFile.getOriginalFilename();

            // Get file extension from original filename
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1); // Without the dot
            } else if (contentType != null) {
                if (contentType.equals("application/pdf")) {
                    fileExtension = "pdf";
                } else if (contentType.contains("image/")) {
                    fileExtension = contentType.substring(contentType.indexOf("/") + 1);
                }
            }

            String resourceType = "image"; // default for images
            String folder = "images";
            if (contentType != null && contentType.toLowerCase().contains("pdf")) {
                resourceType = "raw"; // Use "raw" for PDFs
                folder = "documents";
            }

            // Generate base filename WITHOUT extension
            String timestamp = String.valueOf(System.currentTimeMillis());
            String baseFilename = "file_" + timestamp;

            // Create a public_id that INCLUDES the extension
            String publicId = baseFilename + "." + fileExtension;

            log.info("Uploading to Cloudinary with public_id: {}", publicId);

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "overwrite", true,
                    "resource_type", resourceType,
                    "use_filename", true,
                    "chunk_size", 20000000, // 20MB chunks
//                    "timeout", 120,
                    "eager_async", true,
                    "quality", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(
                    multipartFile.getBytes(),
                    uploadParams
            );

            fileUrl = uploadResult.get("secure_url").toString();

            log.info("File uploaded successfully. URL: {}", fileUrl);

            UploadImageResponse response = UploadImageResponse.builder()
                    .imageUrl(fileUrl).build();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(createSuccessResponse(response, "File upload successful"));

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createFailureResponse("upload_error", "Could not upload file: " + e.getMessage()));
        }
    }
}
