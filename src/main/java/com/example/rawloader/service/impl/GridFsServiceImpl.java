package com.example.rawloader.service.impl;

import com.example.rawloader.repository.RawLoaderMetadataRepository;
import com.example.rawloader.service.api.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GridFsServiceImpl implements FileStorageService {

    private final GridFsTemplate gridFsTemplate;
    private final RawLoaderMetadataRepository metadataRepository;

    @Override
    public String store(String filename, String contentType, InputStream stream) {
        ObjectId id = gridFsTemplate.store(stream, filename, contentType);
        return id.toHexString();
    }

    @Override
    public InputStream downloadById(String gridFsId) {
        GridFsResource resource = getResourceByGridFsId(gridFsId);
        try {
            return resource.getInputStream();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading GridFS resource", e);
        }
    }

    @Override
    public InputStream downloadByMetadataId(String metadataId) {
        var metadata = metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metadata not found"));
        return downloadById(metadata.getGridFsId());
    }

    private GridFsResource getResourceByGridFsId(String gridFsId) {
        try {
            Query q = Query.query(Criteria.where("_id").is(new ObjectId(gridFsId)));
            GridFsResource res = gridFsTemplate.getResource(gridFsTemplate.findOne(q));
            if (res == null || !res.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in GridFS: " + gridFsId);
            }
            return res;
        } catch (IllegalArgumentException iae) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid GridFS id: " + gridFsId, iae);
        }
    }
}
