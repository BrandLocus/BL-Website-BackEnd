package com.hvc.brandlocus.controllers.image;

import com.hvc.brandlocus.services.CloudinaryService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ImageController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<?>> uploadReceipt(@RequestParam MultipartFile file){
        log.info("This is the file:{}",file.getOriginalFilename());
        return cloudinaryService.uploadFile(file);

    }
}
