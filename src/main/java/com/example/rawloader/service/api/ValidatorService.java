package com.example.rawloader.service.api;

import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.ValidationError;

import java.io.InputStream;
import java.util.List;

public interface ValidatorService {
    /**
     * Validate Excel InputStream against the provided config.
     * Returns a list of validation errors; if empty => valid.
     */
    List<ValidationError> validate(InputStream excelInputStream, LoaderConfigDTO config);
}
