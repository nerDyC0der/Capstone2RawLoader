package com.example.rawloader.controller;

import com.example.rawloader.model.RawLoaderTransformed;
import com.example.rawloader.repository.RawLoaderTransformedRepository;
import com.example.rawloader.service.api.TransformService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/raw-loader")
@RequiredArgsConstructor
public class RawLoaderTransformController {

    private final TransformService transformService;
    private final RawLoaderTransformedRepository transformedRepository;

    // Run transformation and return count
    @PostMapping("/transform/{metadataId}")
    public ResponseEntity<Map<String, Object>> transform(@PathVariable String metadataId) {
        int inserted = transformService.transform(metadataId);
        return ResponseEntity.ok(Map.of(
                "metadataId", metadataId,
                "inserted", inserted,
                "message", "Transformation completed"
        ));
    }

    // Preview first N rows without writing
    @GetMapping("/preview/{metadataId}")
    public ResponseEntity<List<Map<String, Object>>> preview(
            @PathVariable String metadataId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transformService.preview(metadataId, limit));
    }

    // Fetch transformed rows written to DB
    @GetMapping("/transformed/{metadataId}")
    public ResponseEntity<List<RawLoaderTransformed>> getTransformed(@PathVariable String metadataId) {
        return ResponseEntity.ok(transformedRepository.findByMetadataId(metadataId));
    }
}
