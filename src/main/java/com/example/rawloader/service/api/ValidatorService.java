package com.example.rawloader.service.api;

import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.ValidationError;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;

public interface ValidatorService {
    /**
     * Validate Excel InputStream against config; returns list of validation errors. If empty => valid.
     */
    List<ValidationError> validate(InputStream excelInputStream, LoaderConfigDTO config);
}
