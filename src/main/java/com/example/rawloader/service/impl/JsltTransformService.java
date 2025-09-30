package com.example.rawloader.service.impl;

import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.service.api.TransformService;
import com.example.rawloader.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory; // ✅ Added import
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple transform preview: maps headers -> canonical keys and produces JSON string per row.
 * Placeholder for JSLT-based transformation.
 */
@Service
@Slf4j
public class JsltTransformService implements TransformService {

    @Override
    public List<String> transformPreview(InputStream excelInputStream, LoaderConfigDTO config) {
        List<String> result = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(excelInputStream)) { // ✅ Now recognized
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return result;

            Row headerRow = sheet.getRow(0);
            List<String> headers = ExcelUtils.readHeader(headerRow);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) headerIndex.put(headers.get(i), i);

            List<Row> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || ExcelUtils.isRowEmpty(row)) continue;
                rows.add(row);
            }

            int previewLimit = Math.min(20, rows.size());
            for (int i = 0; i < previewLimit; i++) {
                Row row = rows.get(i);
                Map<String, Object> mapped = new LinkedHashMap<>();
                for (var col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader());
                    Object val = null;
                    if (idx != null) {
                        val = ExcelUtils.readCellValue(row.getCell(idx));
                        if ("number".equalsIgnoreCase(col.getType()) && val instanceof String) {
                            String s = ((String) val).replaceAll(",", "").trim();
                            try {
                                val = Double.parseDouble(s);
                            } catch (Exception ignored) {}
                        }
                        if ("date".equalsIgnoreCase(col.getType()) && val instanceof Date) {
                            val = ((Date) val).toInstant().toString();
                        }
                    }
                    mapped.put(col.getKey(), val);
                }
                String json = mapped.entrySet().stream()
                        .map(e -> {
                            Object v = e.getValue();
                            String vs = v == null ? "null" :
                                    (v instanceof Number ? v.toString() :
                                            "\"" + v.toString().replace("\"", "\\\"") + "\"");
                            return "\"" + e.getKey() + "\":" + vs;
                        })
                        .collect(Collectors.joining(",", "{", "}"));
                result.add(json);
            }
        } catch (Exception e) {
            log.error("Transform preview failed", e);
        }
        return result;
    }
}
