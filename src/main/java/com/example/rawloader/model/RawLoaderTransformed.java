package com.example.rawloader.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "raw_loader_transformed_data")
@Data
public class RawLoaderTransformed {
    @Id
    private String id;
    private String metadataId;
    private Map<String, Object> transformedRow;
    private Instant insertedAt = Instant.now();
}
