package com.example.rawloader.repository;

import com.example.rawloader.model.LoaderConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LoaderConfigRepository extends MongoRepository<LoaderConfig, String> {
    Optional<LoaderConfig> findByName(String name);
}
