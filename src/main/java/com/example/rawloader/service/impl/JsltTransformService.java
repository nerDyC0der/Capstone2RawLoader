package com.example.rawloader.service.impl;

import com.example.rawloader.service.api.TransformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Stub JSLT transformer implementation.
 * Currently just a placeholder until JSLT templates are ready.
 * It will not be active in the 'dev' profile — only when you run with profile 'jslt'.
 */
@Slf4j
@Service
@Profile("jslt")
public class JsltTransformService implements TransformService {

    @Override
    public int transform(String metadataId) {
        log.info("🔧 [JsltTransformService] transform() called for metadataId={}", metadataId);
        // TODO: Implement JSLT transformation here
        // Returning 0 for now since transformation is not yet implemented
        return 0;
    }

    @Override
    public List<Map<String, Object>> preview(String metadataId, int limit) {
        log.info("🔧 [JsltTransformService] preview() called for metadataId={}, limit={}", metadataId, limit);
        // TODO: Implement JSLT preview logic here
        // Returning empty list for now as a placeholder
        return Collections.emptyList();
    }
}
