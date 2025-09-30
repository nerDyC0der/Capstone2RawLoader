package com.example.rawloader.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "raw_loader_metadata")
@Data
public class RawLoaderMetadata {
    @Id
    private String id;
    private String fileName;
    private Long partnerId;
    private String configId;
    private Instant uploadDate;
    private String gridFsId;
    private String validationStatus; // VALIDATED | FAILED
    private List<ValidationError> errorMessages;
    private Integer rowCount;
}
