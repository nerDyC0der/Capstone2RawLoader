package com.example.rawloader.service.impl;

import com.example.rawloader.model.LoaderColumnDTO;
import com.example.rawloader.model.LoaderConfigDTO;
import com.example.rawloader.model.ValidationError;
import com.example.rawloader.service.api.ValidatorService;
import com.example.rawloader.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Excel validator using Apache POI.
 * - Checks missing headers
 * - Checks required values
 * - Validates basic types: string / number / date
 * Returns a list of ValidationError; empty list => valid.
 */
@Service
@Slf4j
public class ExcelValidatorService implements ValidatorService {

    @Override
    public List<ValidationError> validate(InputStream excelInputStream, LoaderConfigDTO config) {
        List<ValidationError> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook wb = WorkbookFactory.create(excelInputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                errors.add(new ValidationError(null, "internal", "No sheet found in workbook"));
                return errors;
            }

            // ----- 1) Headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add(new ValidationError(null, "internal", "Missing header row"));
                return errors;
            }

            List<String> headers = ExcelUtils.readHeader(headerRow);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                headerIndex.put(headers.get(i).trim(), i);
            }

            for (LoaderColumnDTO col : config.getColumnMappings()) {
                if (!headerIndex.containsKey(col.getHeader())) {
                    errors.add(new ValidationError(null, col.getHeader(), "Missing header"));
                }
            }
            if (!errors.isEmpty()) return errors;

            // ----- 2) Data rows
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || ExcelUtils.isRowEmpty(row)) continue;

                int displayRow = r + 1; // header row is 1-based in messages

                for (LoaderColumnDTO col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader());
                    if (idx == null) continue;

                    Cell cell = row.getCell(idx);
                    String display = (cell == null) ? "" : formatter.formatCellValue(cell).trim();

                    boolean required = safeBool(col.isRequired());
                    String type = safeLower(col.getType(), "string");
                    String format = (col.getFormat() == null || col.getFormat().isBlank())
                            ? "dd/MM/yyyy" : col.getFormat();

                    // Required check
                    if (required && (display == null || display.isEmpty())) {
                        errors.add(new ValidationError(displayRow, col.getHeader(), "Required value is missing"));
                        continue;
                    }
                    // Optional & empty -> skip type validation
                    if (display == null || display.isEmpty()) continue;

                    // Type checks
                    switch (type) {
                        case "number" -> {
                            if (!isNumeric(cell, display)) {
                                errors.add(new ValidationError(displayRow, col.getHeader(), "Invalid data type (expected number)"));
                            }
                        }
                        case "date" -> {
                            if (!isDate(cell, display, format)) {
                                errors.add(new ValidationError(displayRow, col.getHeader(), "Invalid data type (expected date)"));
                            }
                        }
                        default -> {
                            // string or unknown -> pass
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Validation crashed unexpectedly", e);
            errors.add(new ValidationError(null, "internal", e.getMessage() == null ? "Validation error" : e.getMessage()));
        }

        return errors;
    }

    private boolean isNumeric(Cell cell, String display) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) return true;
            if (cell.getCellType() == CellType.FORMULA &&
                    cell.getCachedFormulaResultType() == CellType.NUMERIC) return true;
        }
        try {
            Double.parseDouble(display.replace(",", ""));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isDate(Cell cell, String display, String pattern) {
        // Excel numeric date
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return true;
            if (cell.getCellType() == CellType.FORMULA &&
                    cell.getCachedFormulaResultType() == CellType.NUMERIC &&
                    DateUtil.isCellDateFormatted(cell)) return true;
        }
        // String parse
        if (display == null || display.isEmpty()) return false;

        if (tryParse(display, pattern)) return true;

        // tolerant fallbacks (optional)
        String[] fallbacks = {"dd/MM/yyyy","d/M/yyyy","yyyy-MM-dd","dd-MM-yyyy","d-M-yyyy"};
        for (String p : fallbacks) {
            if (p.equals(pattern)) continue;
            if (tryParse(display, p)) return true;
        }
        return false;
    }

    private boolean tryParse(String text, String pattern) {
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(text.trim(), f);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean safeBool(Boolean b) { return b != null && b; }
    private String safeLower(String s, String def) { return s == null ? def : s.toLowerCase(Locale.ROOT); }
}
