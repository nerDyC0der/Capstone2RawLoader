package com.example.rawloader.service.api;

import java.io.InputStream;

public interface FileStorageService {
    String store(String filename, String contentType, InputStream stream);
    InputStream downloadById(String gridFsId);
    InputStream downloadByMetadataId(String metadataId);
}
