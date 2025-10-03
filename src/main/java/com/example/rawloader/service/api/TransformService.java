package com.example.rawloader.service.api;

import java.util.List;
import java.util.Map;

public interface TransformService {

    /**
     * Transform a validated file referenced by metadataId,
     * applying mapping from loader config, and insert JSON rows into DB.
     * @return count of inserted rows
     */
    int transform(String metadataId);

    /**
     * Preview transformed data (no DB write).
     * Returns first N transformed rows.
     */
    List<Map<String, Object>> preview(String metadataId, int limit);
}
