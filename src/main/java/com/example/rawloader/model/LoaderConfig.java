package com.example.rawloader.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "loader_configs")
@Data
public class LoaderConfig {
    @Id
    private String id;
    private String name;
    private Long partnerId;
    private String status = "ACTIVE";
    private List<LoaderColumnDTO> mappings;
}
