package com.example.rawloader.service.impl;

import com.example.rawloader.client.ConfigClient;
import com.example.rawloader.client.PartnerClient;
import com.example.rawloader.dto.PartnerDTO;
import com.example.rawloader.dto.UploadResponseDTO;
import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.RawLoaderMetadata;
import com.example.rawloader.model.ValidationError;
import com.example.rawloader.repository.RawLoaderMetadataRepository;
import com.example.rawloader.service.api.FileStorageService;
import com.example.rawloader.service.api.RawLoaderService;
import com.example.rawloader.service.api.TransformService;
import com.example.rawloader.service.api.ValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RawLoaderServiceImpl implements RawLoaderService {

    private final PartnerClient partnerClient;
    private final ConfigClient configClient;
    private final ValidatorService validatorService;
    private final FileStorageService fileStorageService;
    private final RawLoaderMetadataRepository metadataRepository;
    private final TransformService transformService;

    @Override
    public UploadResponseDTO handleUpload(MultipartFile file, Long partnerId, String configId) {
        try {
            // 1) fetch partner
            PartnerDTO partner = partnerClient.getPartner(partnerId);
            if (partner == null) {
                return new UploadResponseDTO(null, false, List.of(new ValidationError(null, "partnerId", "Partner not found")), "Partner not found");
            }

            // 2) fetch config
            LoaderConfigDTO config = configClient.getConfig(partnerId, configId);
            if (config == null) {
                return new UploadResponseDTO(null, false, List.of(new ValidationError(null, "configId", "Config not found")), "Config not found");
            }

            // 3) validate Excel
            try (InputStream is = file.getInputStream()) {
                List<ValidationError> errors = validatorService.validate(is, config);
                // store file in GridFS regardless to keep audit trail
                try (InputStream is2 = file.getInputStream()) {
                    String gridFsId = fileStorageService.store(file.getOriginalFilename(), file.getContentType(), is2);

                    RawLoaderMetadata metadata = new RawLoaderMetadata();
                    metadata.setFileName(file.getOriginalFilename());
                    metadata.setPartnerId(partnerId);
                    metadata.setConfigId(configId);
                    metadata.setUploadDate(Instant.now());
                    metadata.setGridFsId(gridFsId);
                    metadata.setValidationStatus(errors.isEmpty() ? "VALIDATED" : "FAILED");
                    metadata.setErrorMessages(errors);
                    metadata.setRowCount(null); // optional, could compute in validator

                    RawLoaderMetadata saved = metadataRepository.save(metadata);

                    if (!errors.isEmpty()) {
                        return new UploadResponseDTO(saved.getId(), false, errors, "Validation failed");
                    }

                    // Valid: optionally prepare a transformed preview (not persisting here)
                    try (InputStream is3 = file.getInputStream()) {
                        List<String> preview = transformService.transformPreview(is3, config);
                        // We do not persist transformed data here in this flow (commit later)
                    }
                    return new UploadResponseDTO(saved.getId(), true, List.of(), "Uploaded and validated");
                }
            }

        } catch (Exception e) {
            return new UploadResponseDTO(null, false, List.of(new ValidationError(null, "internal", e.getMessage())), "Internal error: " + e.getMessage());
        }
    }

    @Override
    public RawLoaderMetadata getMetadata(String id) {
        return metadataRepository.findById(id).orElseThrow(() -> new RuntimeException("Metadata not found: " + id));
    }
}
