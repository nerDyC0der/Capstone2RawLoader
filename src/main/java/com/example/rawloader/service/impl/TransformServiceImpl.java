package com.example.rawloader.service.impl;

import com.example.rawloader.model.LoaderConfig;
import com.example.rawloader.model.RawLoaderTransformed;
import com.example.rawloader.repository.RawLoaderTransformedRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransformServiceImpl {

    private final RawLoaderTransformedRepository transformedRepository;

    public void transformAndSave(InputStream excelInput, String metadataId, LoaderConfig config) throws Exception {
        Workbook workbook = WorkbookFactory.create(excelInput);
        Sheet sheet = workbook.getSheetAt(0);

        // Read headers
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            headerIndex.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }

        // For each row, map values
        List<RawLoaderTransformed> transformedList = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, Object> jsonRow = new LinkedHashMap<>();
            for (var mapping : config.getMappings()) {
                String header = mapping.getHeader();
                String key = mapping.getKey();
                String type = mapping.getType();
                String format = mapping.getFormat();

                int colIdx = headerIndex.getOrDefault(header, -1);
                if (colIdx == -1) continue;
                Cell cell = row.getCell(colIdx);

                Object value = null;
                if (cell != null) {
                    switch (type.toLowerCase()) {
                        case "string" -> value = cell.getStringCellValue();
                        case "number" -> value = cell.getNumericCellValue();
                        case "date" -> {
                            if (cell.getCellType() == CellType.NUMERIC)
                                value = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                            else if (format != null)
                                value = LocalDate.parse(cell.getStringCellValue(), DateTimeFormatter.ofPattern(format)).toString();
                        }
                    }
                }
                jsonRow.put(key, value);
            }

            RawLoaderTransformed transformed = new RawLoaderTransformed();
            transformed.setMetadataId(metadataId);
            transformed.setTransformedRow(jsonRow);
            transformedList.add(transformed);
        }

        transformedRepository.saveAll(transformedList);
        workbook.close();
    }
}
