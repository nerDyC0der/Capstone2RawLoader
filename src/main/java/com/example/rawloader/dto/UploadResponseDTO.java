package com.example.rawloader.dto;

import com.example.rawloader.model.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UploadResponseDTO {
    private String metadataId;
    private boolean valid;
    private List<ValidationError> errors;
    private String message;
}
