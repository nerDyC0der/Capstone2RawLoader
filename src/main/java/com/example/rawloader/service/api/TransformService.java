package com.example.rawloader.service.api;

import com.example.rawloader.model.LoaderConfigDTO;

import java.io.InputStream;
import java.util.List;

public interface TransformService {
    /**
     * Transform rows using config; return list of transformed JSON strings (preview).
     */
    List<String> transformPreview(InputStream excelInputStream, LoaderConfigDTO config);
}
