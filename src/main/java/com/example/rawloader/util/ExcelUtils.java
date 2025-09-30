package com.example.rawloader.util;

import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelUtils {

    public static List<String> readHeader(Row headerRow) {
        List<String> headers = new ArrayList<>();
        int last = headerRow.getLastCellNum();
        for (int i = 0; i < last; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                headers.add("");
            } else {
                headers.add(cell.getStringCellValue().trim());
            }
        }
        return headers;
    }

    public static boolean isCellEmpty(Cell cell) {
        if (cell == null) return true;
        switch (cell.getCellType()) {
            case BLANK: return true;
            case STRING: return cell.getStringCellValue().trim().isEmpty();
            default: return false;
        }
    }

    public static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        int last = row.getLastCellNum();
        if (last < 0) return true;
        for (int i = 0; i < last; i++) {
            Cell c = row.getCell(i);
            if (c != null && !isCellEmpty(c)) return false;
        }
        return true;
    }

    public static Object readCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) return (long)d;
                    return d;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    } else {
                        double d = cell.getNumericCellValue();
                        if (d == (long) d) return (long)d;
                        return d;
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return null;
        }
    }
}
