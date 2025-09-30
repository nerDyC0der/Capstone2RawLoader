package com.example.rawloader.repository;

import com.example.rawloader.model.RawLoaderTransformed;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RawLoaderTransformedRepository extends MongoRepository<RawLoaderTransformed, String> {
}
