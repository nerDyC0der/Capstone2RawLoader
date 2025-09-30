package com.example.rawloader.model;

import lombok.Data;
import java.util.List;

@Data
public class LoaderConfig {
    private String name;
    private Long partnerId;
    private List<LoaderColumn> mappings;
}
