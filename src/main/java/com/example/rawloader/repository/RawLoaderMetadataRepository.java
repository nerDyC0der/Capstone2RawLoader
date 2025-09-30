package com.example.rawloader.repository;

import com.example.rawloader.model.RawLoaderMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RawLoaderMetadataRepository extends MongoRepository<RawLoaderMetadata, String> {
}
