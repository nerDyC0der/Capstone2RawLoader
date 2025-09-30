package com.example.rawloader.controller;

import com.example.rawloader.model.LoaderConfig;
import com.example.rawloader.repository.LoaderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final LoaderConfigRepository repository;

    // POST /configs → create config
    @PostMapping
    public ResponseEntity<LoaderConfig> create(@RequestBody LoaderConfig config) {
        LoaderConfig saved = repository.save(config);
        return ResponseEntity.ok(saved);
    }

    // GET /configs → list all
    @GetMapping
    public List<LoaderConfig> list() {
        return repository.findAll();
    }

    // GET /configs/{id} → get by ID
    @GetMapping("/{id}")
    public ResponseEntity<LoaderConfig> getById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /configs/by-name/{name} → find by name
    @GetMapping("/by-name/{name}")
    public ResponseEntity<LoaderConfig> getByName(@PathVariable String name) {
        return repository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
