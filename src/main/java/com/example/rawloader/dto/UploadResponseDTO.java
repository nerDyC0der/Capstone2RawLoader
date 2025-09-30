package com.example.rawloader.dto;

import com.example.rawloader.model.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor        // <-- add this
@AllArgsConstructor
public class UploadResponseDTO {
    private String metadataId;
    private boolean valid;
    private List<ValidationError> errors;
    private String message;
    // Optional: add fileName / validationStatus if you like
    private String fileName;
    private String validationStatus;
}
