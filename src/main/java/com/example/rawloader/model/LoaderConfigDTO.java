package com.example.rawloader.model;

import lombok.Data;

import java.util.List;

/**
 * DTO returned by Config Service
 */
@Data
public class LoaderConfigDTO {
    private String configId;
    private Long partnerId;
    private String name;
    private String status;
    private List<LoaderColumnDTO> columnMappings;
    // jslt, postTransformJsonSchema etc can be added as needed
}
