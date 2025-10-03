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

            // 1️⃣ Header validation
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add(new ValidationError(null, "internal", "Missing header row"));
                return errors;
            }

            List<String> headers = ExcelUtils.readHeader(headerRow);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                headerIndex.put(headers.get(i).trim().toLowerCase(Locale.ROOT), i);
            }

            for (LoaderColumnDTO col : config.getColumnMappings()) {
                if (!headerIndex.containsKey(col.getHeader().trim().toLowerCase(Locale.ROOT))) {
                    errors.add(new ValidationError(null, col.getHeader(), "Missing header in Excel"));
                }
            }
            if (!errors.isEmpty()) return errors;

            // 2️⃣ Row validation
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || ExcelUtils.isRowEmpty(row)) continue;
                int displayRow = r + 1; // Excel rows are 1-based for users

                for (LoaderColumnDTO col : config.getColumnMappings()) {
                    Integer idx = headerIndex.get(col.getHeader().trim().toLowerCase(Locale.ROOT));
                    if (idx == null) continue;

                    Cell cell = row.getCell(idx);
                    String displayValue = (cell == null) ? "" : formatter.formatCellValue(cell).trim();

                    boolean required = col.isRequired();
                    String type = (col.getType() == null) ? "string" : col.getType().toLowerCase(Locale.ROOT);
                    String format = (col.getFormat() == null || col.getFormat().isBlank()) ? "dd/MM/yyyy" : col.getFormat();

                    // 🟠 Required check
                    if (required && (displayValue == null || displayValue.isEmpty())) {
                        errors.add(new ValidationError(displayRow, col.getHeader(), "Required value is missing"));
                        continue;
                    }

                    // 🟢 Skip empty optional
                    if (displayValue == null || displayValue.isEmpty()) continue;

                    // 🔵 Type validation
                    switch (type) {
                        case "number" -> {
                            if (!isNumeric(cell, displayValue)) {
                                errors.add(new ValidationError(displayRow, col.getHeader(), "Invalid data type (expected number)"));
                            }
                        }
                        case "date" -> {
                            if (!isDate(cell, displayValue, format)) {
                                errors.add(new ValidationError(displayRow, col.getHeader(),
                                        "Invalid date format (expected " + format + ")"));
                            }
                        }
                        default -> {
                            // string → always valid
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Validation crashed unexpectedly", e);
            errors.add(new ValidationError(null, "internal", e.getMessage() != null ? e.getMessage() : "Validation failed"));
        }

        return errors;
    }

    private boolean isNumeric(Cell cell, String display) {
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC) return true;
            if (cell != null && cell.getCellType() == CellType.FORMULA &&
                    cell.getCachedFormulaResultType() == CellType.NUMERIC) return true;
            Double.parseDouble(display.replace(",", ""));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDate(Cell cell, String display, String pattern) {
        try {
            // Excel-native date
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return true;
                if (cell.getCellType() == CellType.FORMULA &&
                        cell.getCachedFormulaResultType() == CellType.NUMERIC &&
                        DateUtil.isCellDateFormatted(cell)) return true;
            }
            // Try parse as text
            if (tryParse(display, pattern)) return true;

            // fallback formats
            String[] fallbacks = {"dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy"};
            for (String f : fallbacks) {
                if (tryParse(display, f)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
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
}
