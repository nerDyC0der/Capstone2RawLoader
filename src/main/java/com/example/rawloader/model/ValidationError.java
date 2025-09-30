package com.example.rawloader.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError {
    private Integer row;
    private String field;
    private String message;
}
