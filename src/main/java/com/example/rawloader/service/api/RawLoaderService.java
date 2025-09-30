package com.example.rawloader.service.api;

import com.example.rawloader.dto.UploadResponseDTO;
import com.example.rawloader.model.RawLoaderMetadata;
import org.springframework.web.multipart.MultipartFile;

public interface RawLoaderService {
    UploadResponseDTO handleUpload(MultipartFile file, Long partnerId, String configId);
    RawLoaderMetadata getMetadata(String id);
}
