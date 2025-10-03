package com.example.rawloader.service.api;

import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.ValidationError;

import java.io.InputStream;
import java.util.List;

public interface ValidatorService {

    List<ValidationError> validate(InputStream excelInputStream, LoaderConfigDTO config);
}
