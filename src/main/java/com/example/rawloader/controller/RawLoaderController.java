package com.example.rawloader.controller;

import com.example.rawloader.dto.UploadResponseDTO;
import com.example.rawloader.exception.FileValidationException;
import com.example.rawloader.model.RawLoaderMetadata;
import com.example.rawloader.service.api.FileStorageService;
import com.example.rawloader.service.api.RawLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/raw-loader")
@RequiredArgsConstructor
public class RawLoaderController {

    private final RawLoaderService rawLoaderService;
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("partnerId") Long partnerId,
            @RequestParam("configId") String configId
    ) {
        UploadResponseDTO resp = rawLoaderService.handleUpload(file, partnerId, configId);
        if (!resp.isValid()) {
            return ResponseEntity.badRequest().body(resp);
        }
        return ResponseEntity.accepted().body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RawLoaderMetadata> getMetadata(@PathVariable("id") String id) {
        RawLoaderMetadata metadata = rawLoaderService.getMetadata(id);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable("id") String id) {
        InputStream is = fileStorageService.downloadByMetadataId(id);
        RawLoaderMetadata metadata = rawLoaderService.getMetadata(id);
        InputStreamResource resource = new InputStreamResource(is);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
