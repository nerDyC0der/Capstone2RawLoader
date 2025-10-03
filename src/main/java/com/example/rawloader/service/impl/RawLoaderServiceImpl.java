package com.example.rawloader.service.impl;

import com.example.rawloader.client.ConfigClient;
import com.example.rawloader.client.PartnerClient;
import com.example.rawloader.dto.PartnerDTO;
import com.example.rawloader.dto.UploadResponseDTO;
import com.example.rawloader.exception.FileValidationException;
import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.RawLoaderMetadata;
import com.example.rawloader.model.ValidationError;
import com.example.rawloader.repository.RawLoaderMetadataRepository;
import com.example.rawloader.service.api.FileStorageService;
import com.example.rawloader.service.api.RawLoaderService;
import com.example.rawloader.service.api.ValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawLoaderServiceImpl implements RawLoaderService {

    private final PartnerClient partnerClient;
    private final ConfigClient configClient;
    private final ValidatorService validatorService;
    private final FileStorageService fileStorageService;
    private final RawLoaderMetadataRepository metadataRepository;

    @Override
    public UploadResponseDTO handleUpload(MultipartFile file, Long partnerId, String configId) {
        try {
            log.info("Upload start partnerId={}, configId={}, file={}", partnerId, configId, file.getOriginalFilename());

            PartnerDTO partner = partnerClient.getPartner(partnerId);
            LoaderConfigDTO config = configClient.getConfig(partnerId, configId);

            RawLoaderMetadata metadata = new RawLoaderMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setPartnerId(partnerId);
            metadata.setConfigId(configId);
            metadata.setUploadDate(Instant.now());

            // ✅ Always store the file in GridFS first
            String gridFsId;
            try (InputStream in = file.getInputStream()) {
                gridFsId = fileStorageService.store(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        in
                );
            }
            metadata.setGridFsId(gridFsId);

            // ✅ Validate Excel
            List<ValidationError> errors;
            try (InputStream in = file.getInputStream()) {
                errors = validatorService.validate(in, config);
            }

            if (!errors.isEmpty()) {
                metadata.setValidationStatus("FAILED");
                metadata.setErrorMessages(errors);
                metadataRepository.save(metadata);
                return new UploadResponseDTO(
                        metadata.getId(), false, errors, "Validation failed",
                        metadata.getFileName(), metadata.getValidationStatus()
                );
            }

            metadata.setValidationStatus("VALIDATED");
            metadataRepository.save(metadata);

            return new UploadResponseDTO(
                    metadata.getId(), true, List.of(),
                    "File validated and stored successfully",
                    metadata.getFileName(), metadata.getValidationStatus()
            );

        } catch (FileValidationException e) {
            log.error("Validation failed", e);
            return new UploadResponseDTO(null, false, List.of(new ValidationError(null, "validation", e.getMessage())),
                    "Validation failed", file.getOriginalFilename(), "FAILED");
        } catch (Exception e) {
            log.error("Upload failed", e);
            return new UploadResponseDTO(null, false, List.of(new ValidationError(null, "internal", e.getMessage())),
                    "Internal error", file.getOriginalFilename(), "FAILED");
        }
    }

    // ✅ Implement the missing method
    @Override
    public RawLoaderMetadata getMetadata(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metadata not found for id: " + id));
    }
}
