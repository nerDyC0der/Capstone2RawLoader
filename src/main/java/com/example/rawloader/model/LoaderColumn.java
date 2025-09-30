package com.example.rawloader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaderColumn {
    private String header;
    private String key;
    private String type;
    private boolean required;
    private String format;
}
