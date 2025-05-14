package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import vn.com.fecredit.app.service.ImportFileProcessor;
import vn.com.fecredit.app.service.dto.ImportError;
import vn.com.fecredit.app.service.dto.ImportValidator;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.validator.ImportValidatorFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Service for processing import files including validation and data extraction.
 * Supports CSV and Excel file formats.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportFileProcessorImpl implements ImportFileProcessor {

    private final ImportValidatorFactory validatorFactory;

    /**
     * Count rows in an Excel file
     *
     * @param filePath Path to Excel file
     * @return Number of data rows (excluding header)
     * @throws IOException if file cannot be read
     */
    public int countExcelRows(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            // Subtract 1 to exclude header row
            return Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
        } catch (Exception e) {
            log.error("Error counting Excel rows: {}", e.getMessage());
            throw new IOException("Failed to count rows in Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Validate file based on entity type
     *
     * @param filePath Path to file
     * @param objectType Entity type being imported
     * @param validationCallback Callback function for validation errors
     * @throws Exception if validation fails
     */
    public void validateFile(String filePath, ObjectType objectType,
            BiFunction<Integer, ImportError, Boolean> validationCallback) throws Exception {

        // Get the validator for this entity type
        ImportValidator validator = validatorFactory.getValidator(objectType);

        if (validator == null) {
            throw new IllegalArgumentException("No validator found for object type: " + objectType);
        }

        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".csv")) {
            validateCsvFile(filePath, validator, validationCallback);
        } else if (lowerPath.endsWith(".xlsx") || lowerPath.endsWith(".xls")) {
            validateExcelFile(filePath, validator, validationCallback);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }

    /**
     * Validate a CSV file using Apache Commons CSV
     */
    private void validateCsvFile(String filePath, ImportValidator validator,
            BiFunction<Integer, ImportError, Boolean> validationCallback) throws Exception {

        try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .parse(Files.newBufferedReader(Paths.get(filePath)))) {

            // Validate header fields
            List<String> headerFields = new ArrayList<>(parser.getHeaderNames());

            if (headerFields.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            List<ImportError> headerErrors = validator.validateHeaders(headerFields);

            for (ImportError error : headerErrors) {
                if (!validationCallback.apply(0, error)) {
                    return; // Stop validation if callback returns false
                }
            }

            // Process each data row
            int rowNumber = 1;

            for (CSVRecord record : parser) {
                // Convert row to a map of field name -> value
                Map<String, String> rowData = new HashMap<>();

                for (String header : headerFields) {
                    rowData.put(header, record.get(header));
                }

                // Validate the row
                List<ImportError> rowErrors = validator.validateRow(rowData, rowNumber);

                // Process each error through callback
                for (ImportError error : rowErrors) {
                    if (!validationCallback.apply(rowNumber, error)) {
                        return; // Stop validation if callback returns false
                    }
                }

                rowNumber++;
            }
        }
    }

    /**
     * Validate an Excel file
     */
    private void validateExcelFile(String filePath, ImportValidator validator,
            BiFunction<Integer, ImportError, Boolean> validationCallback) throws Exception {

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file has no header row");
            }

            // Extract header fields
            List<String> headerFields = new ArrayList<>();
            for (Cell cell : headerRow) {
                headerFields.add(dataFormatter.formatCellValue(cell));
            }

            // Validate headers
            List<ImportError> headerErrors = validator.validateHeaders(headerFields);
            for (ImportError error : headerErrors) {
                if (!validationCallback.apply(0, error)) {
                    return; // Stop validation if callback returns false
                }
            }

            // Process each data row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                // Skip completely empty rows
                if (row == null) {
                    continue;
                }

                // Convert row to a map of field name -> value
                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headerFields.size(); j++) {
                    Cell cell = row.getCell(j);
                    String value = (cell == null) ? "" : dataFormatter.formatCellValue(cell);
                    rowData.put(headerFields.get(j), value);
                }

                // Validate the row
                List<ImportError> rowErrors = validator.validateRow(rowData, i + 1);

                // Process each error through callback
                for (ImportError error : rowErrors) {
                    if (!validationCallback.apply(i + 1, error)) {
                        return; // Stop validation if callback returns false
                    }
                }
            }
        }
    }
}
