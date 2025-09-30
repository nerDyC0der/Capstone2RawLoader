package com.example.rawloader.service.impl;

import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.LoaderColumnDTO;
import com.example.rawloader.model.ValidationError;
import com.example.rawloader.service.api.ValidatorService;
import com.example.rawloader.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ExcelValidatorService implements ValidatorService {

    private static final int HEADER_ROW_INDEX = 0;

    @Override
    public List<ValidationError> validate(InputStream excelInputStream, LoaderConfigDTO config) {
        List<ValidationError> errors = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(excelInputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                errors.add(new ValidationError(null, "sheet", "No sheets found"));
                return errors;
            }

            Row headerRow = sheet.getRow(HEADER_ROW_INDEX);
            if (headerRow == null) {
                errors.add(new ValidationError(null, "header", "Header row missing"));
                return errors;
            }

            List<String> sheetHeaders = ExcelUtils.readHeader(headerRow);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < sheetHeaders.size(); i++) headerIndex.put(sheetHeaders.get(i), i);

            // check expected headers exist
            for (LoaderColumnDTO col : config.getColumnMappings()) {
                if (!headerIndex.containsKey(col.getHeader())) {
                    errors.add(new ValidationError(null, col.getHeader(), "Missing header"));
                }
            }

            if (!errors.isEmpty()) return errors;

            // iterate rows
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                // sparse heuristics: skip fully blank rows
                if (ExcelUtils.isRowEmpty(row)) continue;

                for (LoaderColumnDTO col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader());
                    if (idx == null) continue;
                    Cell cell = row.getCell(idx);
                    boolean emptyCell = (cell == null || ExcelUtils.isCellEmpty(cell));
                    if (col.isRequired() && emptyCell) {
                        errors.add(new ValidationError(r + 1, col.getHeader(), "Required column is empty"));
                        continue;
                    }
                    if (!emptyCell) {
                        boolean ok = validateCellType(cell, col);
                        if (!ok) {
                            errors.add(new ValidationError(r + 1, col.getHeader(), "Invalid data type (expected " + col.getType() + ")"));
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Validation failed", e);
            errors.add(new ValidationError(null, "internal", e.getMessage()));
        }
        return errors;
    }

    private boolean validateCellType(Cell cell, LoaderColumnDTO col) {
        String t = col.getType() == null ? "string" : col.getType().toLowerCase();
        try {
            switch (t) {
                case "number":
                case "numeric":
                    if (cell.getCellType() == CellType.NUMERIC) return true;
                    if (cell.getCellType() == CellType.STRING) {
                        String s = cell.getStringCellValue().replaceAll(",", "").trim();
                        try {
                            Double.parseDouble(s);
                            return true;
                        } catch (NumberFormatException nfe) {
                            return false;
                        }
                    }
                    return false;
                case "date":
                    if (DateUtil.isCellDateFormatted(cell)) return true;
                    if (cell.getCellType() == CellType.STRING) {
                        String fmt = col.getFormat();
                        if (fmt == null || fmt.isBlank()) {
                            // try ISO-like
                            try {
                                LocalDate.parse(cell.getStringCellValue());
                                return true;
                            } catch (Exception ex) {
                                return false;
                            }
                        } else {
                            try {
                                DateTimeFormatter df = DateTimeFormatter.ofPattern(fmt);
                                LocalDate.parse(cell.getStringCellValue(), df);
                                return true;
                            } catch (Exception ex) {
                                return false;
                            }
                        }
                    }
                    return false;
                case "boolean":
                    if (cell.getCellType() == CellType.BOOLEAN) return true;
                    if (cell.getCellType() == CellType.STRING) {
                        String v = cell.getStringCellValue().trim().toLowerCase();
                        return List.of("true","false","y","n","1","0").contains(v);
                    }
                    return false;
                case "string":
                default:
                    // any value is acceptable for string; prefer convertible
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
