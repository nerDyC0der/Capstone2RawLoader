package com.example.rawloader.exception;

import com.example.rawloader.model.ValidationError;
import lombok.Getter;

import java.util.List;

@Getter
public class FileValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public FileValidationException(String message) {
        super(message);
        this.errors = List.of(new ValidationError(null, "internal", message));
    }

    public FileValidationException(List<ValidationError> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
