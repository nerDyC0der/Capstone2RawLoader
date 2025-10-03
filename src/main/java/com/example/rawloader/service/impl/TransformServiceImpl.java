package com.example.rawloader.service.impl;

import com.example.rawloader.client.ConfigClient;
import com.example.rawloader.model.LoaderColumnDTO;
import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.RawLoaderMetadata;
import com.example.rawloader.model.RawLoaderTransformed;
import com.example.rawloader.repository.RawLoaderMetadataRepository;
import com.example.rawloader.repository.RawLoaderTransformedRepository;
import com.example.rawloader.service.api.FileStorageService;
import com.example.rawloader.service.api.TransformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransformServiceImpl implements TransformService {

    private final RawLoaderMetadataRepository metadataRepository;
    private final RawLoaderTransformedRepository transformedRepository;
    private final FileStorageService fileStorageService;
    private final ConfigClient configClient;

    @Override
    public int transform(String metadataId) {
        RawLoaderMetadata metadata = metadataRepository.findById(metadataId)
                .orElseThrow(() -> new RuntimeException("Metadata not found: " + metadataId));

        LoaderConfigDTO config = configClient.getConfig(metadata.getPartnerId(), metadata.getConfigId());

        try (InputStream in = fileStorageService.downloadByMetadataId(metadataId);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new RuntimeException("No sheet found in workbook");

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("No header row found");

            Map<String, Integer> headerIndex = new HashMap<>();
            for (Cell c : headerRow) {
                headerIndex.put(c.getStringCellValue().trim(), c.getColumnIndex());
            }

            DataFormatter formatter = new DataFormatter();
            List<RawLoaderTransformed> toInsert = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, Object> transformedRow = new LinkedHashMap<>();
                for (LoaderColumnDTO col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader());
                    if (idx == null) {
                        transformedRow.put(col.getKey(), null);
                        continue;
                    }
                    Cell cell = row.getCell(idx);
                    String shown = formatter.formatCellValue(cell);
                    Object value = convertValue(cell, shown, col.getType(), col.getFormat());
                    transformedRow.put(col.getKey(), value);
                }

                RawLoaderTransformed doc = new RawLoaderTransformed();
                doc.setMetadataId(metadataId);
                doc.setTransformedRow(transformedRow);
                toInsert.add(doc);
            }

            transformedRepository.saveAll(toInsert);
            return toInsert.size();

        } catch (Exception e) {
            log.error("Transformation failed for {}: {}", metadataId, e.getMessage(), e);
            throw new RuntimeException("Transformation failed: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> preview(String metadataId, int limit) {
        RawLoaderMetadata metadata = metadataRepository.findById(metadataId)
                .orElseThrow(() -> new RuntimeException("Metadata not found: " + metadataId));

        LoaderConfigDTO config = configClient.getConfig(metadata.getPartnerId(), metadata.getConfigId());

        try (InputStream in = fileStorageService.downloadByMetadataId(metadataId);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (Cell c : headerRow) headerIndex.put(c.getStringCellValue().trim(), c.getColumnIndex());

            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> preview = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum() && preview.size() < limit; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, Object> transformedRow = new LinkedHashMap<>();
                for (LoaderColumnDTO col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader());
                    Cell cell = idx != null ? row.getCell(idx) : null;
                    String shown = (cell == null) ? "" : formatter.formatCellValue(cell);
                    Object value = convertValue(cell, shown, col.getType(), col.getFormat());
                    transformedRow.put(col.getKey(), value);
                }
                preview.add(transformedRow);
            }

            return preview;

        } catch (Exception e) {
            log.error("Preview failed: {}", e.getMessage(), e);
            throw new RuntimeException("Preview failed: " + e.getMessage());
        }
    }

    private Object convertValue(Cell cell, String shown, String type, String format) {
        if (type == null) return shown;
        switch (type.toLowerCase()) {
            case "number":
                try { return Double.parseDouble(shown); } catch (Exception e) { return null; }
            case "date":
                try {
                    if (cell != null && DateUtil.isCellDateFormatted(cell))
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    DateTimeFormatter f = DateTimeFormatter.ofPattern(format != null ? format : "dd/MM/yyyy");
                    return LocalDate.parse(shown, f).toString();
                } catch (Exception e) { return null; }
            default:
                return shown;
        }
    }
}
