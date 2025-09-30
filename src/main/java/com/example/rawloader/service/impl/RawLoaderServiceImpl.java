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
import com.example.rawloader.service.api.ValidatorService;
import com.example.rawloader.service.api.RawLoaderService;
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

    private final PartnerClient partnerClient;                 // uses dev stub in 'dev' profile
    private final ConfigClient configClient;                   // uses dev stub in 'dev' profile
    private final ValidatorService validatorService;
    private final FileStorageService fileStorageService;
    private final RawLoaderMetadataRepository metadataRepository;
    private final TransformServiceImpl transformService;       // if you integrated transform step

    @Override
    public UploadResponseDTO handleUpload(MultipartFile file, Long partnerId, String configId) {
        try {
            log.info("Upload start partnerId={}, configId={}, file={}", partnerId, configId, file.getOriginalFilename());

            // 1) Fetch partner + config (stubbed in 'dev')
            PartnerDTO partner = partnerClient.getPartner(partnerId);
            LoaderConfigDTO config = configClient.getConfig(partnerId, configId);

            // 2) Validate
            List<ValidationError> errors;
            try (InputStream vin = file.getInputStream()) {
                errors = validatorService.validate(vin, config);
            }

            // 3) Prepare metadata (we will fill gridFsId after store if valid)
            RawLoaderMetadata md = new RawLoaderMetadata();
            md.setFileName(file.getOriginalFilename());
            md.setPartnerId(partnerId);
            md.setConfigId(configId);
            md.setUploadDate(Instant.now());

            if (!errors.isEmpty()) {
                // 4a) Validation FAILED
                md.setValidationStatus("FAILED");
                md.setErrorMessages(errors);
                metadataRepository.save(md);

                return new UploadResponseDTO(
                        md.getId(),
                        false,
                        errors,
                        "Validation failed",
                        md.getFileName(),
                        md.getValidationStatus()
                );
            }

            // 4b) Validation PASSED → store file in GridFS
            String gridFsId;
            try (InputStream sin = file.getInputStream()) {
                gridFsId = fileStorageService.store(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        sin
                );
            }
            md.setGridFsId(gridFsId);
            md.setValidationStatus("VALIDATED");
            metadataRepository.save(md);

            // 5) (Optional) Transform rows and save
            // try (InputStream tin = file.getInputStream()) {
            //     transformService.transformAndSave(tin, md.getId(), config);
            // }

            return new UploadResponseDTO(
                    md.getId(),
                    true,
                    List.of(),
                    "File validated and stored successfully",
                    md.getFileName(),
                    md.getValidationStatus()
            );

        } catch (Exception e) {
            log.error("Upload failed", e);
            // Return a safe error payload
            return new UploadResponseDTO(
                    null,
                    false,
                    List.of(new ValidationError(null, "internal", e.getMessage() == null ? "Internal error" : e.getMessage())),
                    "Internal error",
                    file != null ? file.getOriginalFilename() : null,
                    "FAILED"
            );
        }
    }

    @Override
    public RawLoaderMetadata getMetadata(String id) {
        return metadataRepository.findById(id).orElse(null);
    }
}
