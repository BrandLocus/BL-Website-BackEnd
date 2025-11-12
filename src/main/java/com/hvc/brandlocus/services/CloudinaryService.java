package com.hvc.brandlocus.services;

import com.hvc.brandlocus.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    ResponseEntity<ApiResponse<?>> uploadFile(MultipartFile multipartFile);

}
