package com.example.rawloader.model;

import lombok.Data;

import java.util.List;

/**
 * Represents one column mapping in a loader transformation config
 */
@Data
public class LoaderColumnDTO {
    private String header;     // header name in Excel
    private String key;        // canonical key
    private String type;       // string|number|date|boolean
    private boolean required;
    private String format;     // date format or regex
    private List<String> enumValues;
}
