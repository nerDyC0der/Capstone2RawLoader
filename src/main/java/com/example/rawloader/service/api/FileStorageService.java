package com.example.rawloader.service.api;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * Store a file into GridFS.
     * @return gridFsId (hex string)
     */
    String store(String filename, String contentType, InputStream stream);

    /**
     * Download raw file by GridFS id.
     */
    InputStream downloadById(String gridFsId);

    /**
     * Download raw file by metadata id (service will look up metadata.gridFsId).
     */
    InputStream downloadByMetadataId(String metadataId);
}
