package com.example.rawloader.repository;

import com.example.rawloader.model.RawLoaderTransformed;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RawLoaderTransformedRepository extends MongoRepository<RawLoaderTransformed, String> {
    List<RawLoaderTransformed> findByMetadataId(String metadataId);
}
