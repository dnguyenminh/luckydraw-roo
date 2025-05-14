package vn.com.fecredit.app.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.config.FileStorageProperties;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.dto.UploadFile;

/**
 * Service responsible for handling the export of table data to Excel files
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExportService {

    private final FileStorageProperties fileStorageProperties;

    // Helper class for column mapping
    @Getter
    @AllArgsConstructor
    private static class ColumnMapping {
        private final String displayName;
        private final String fieldKey;
        private final int columnIndex;
    }

    /**
     * Process an EXPORT action request
     */
    public TableActionResponse processExportAction(TableActionRequest request, TableDataServiceImpl tableDataService) {
        try {
            // Generate a unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String baseFilename = request.getObjectType() + "_" + timestamp;
            String extractingFilename = baseFilename + ".extracting.xlsx";
            String completeFilename = baseFilename + ".xlsx";
            String failedFilename = baseFilename + ".failed.txt";

            // Use the configured path from properties
            Path tempDir = fileStorageProperties.getExportsPath();
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            Path extractingFilePath = tempDir.resolve(extractingFilename);
            Files.createFile(extractingFilePath);
            Path completeFilePath = tempDir.resolve(completeFilename);
            Path failedFilePath = tempDir.resolve(failedFilename);

            // Create a CompletableFuture for the export operation
            CompletableFuture.runAsync(() -> {
                try {
                    // First fetch the data to export
                    TableFetchResponse fetchResponse = fetchDataForExport(request, tableDataService);
                    
                    if (fetchResponse.getStatus() != FetchStatus.SUCCESS &&
                            fetchResponse.getStatus() != FetchStatus.NO_DATA) {
                        throw new RuntimeException("Failed to fetch data: " + fetchResponse.getMessage());
                    }

                    // Write data to Excel
                    writeExcelFile(extractingFilePath, request, fetchResponse);

                    // If successful, rename to final filename
                    Files.move(extractingFilePath, completeFilePath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Export completed successfully: {}", completeFilePath);

                } catch (Exception e) {
                    handleExportError(e, extractingFilePath, failedFilePath);
                }
            });

            // Create the download info for immediate response
            UploadFile downloadFile = new UploadFile();
            downloadFile.setFileName(completeFilename);

            // Return response immediately
            TableActionResponse response = TableActionResponse.success(request, 
                "Export started. File will be available for download when ready.", 
                null);  // Use factory method instead of constructor
            
            // Set the download file on the response
            response.setDownloadFile(downloadFile);
            return response;

        } catch (Exception e) {
            log.error("Error initiating export action", e);
            return TableActionResponse.error(request, "Failed to start export: " + e.getMessage());
        }
    }
    
    /**
     * Fetch data for export using table data service
     */
    private TableFetchResponse fetchDataForExport(TableActionRequest request, TableDataServiceImpl tableDataService) {
        TableFetchRequest fetchRequest = new TableFetchRequest();
        fetchRequest.setObjectType(request.getObjectType());
        fetchRequest.setEntityName(request.getEntityName());
        fetchRequest.setPage(0);
        fetchRequest.setSize(Integer.MAX_VALUE);
        fetchRequest.setSorts(request.getSorts());
        fetchRequest.setFilters(request.getFilters());
        fetchRequest.setSearch(request.getSearch());
        
        return tableDataService.fetchData(fetchRequest);
    }
    
    /**
     * Write data to Excel file
     */
    private void writeExcelFile(Path filePath, TableActionRequest request, TableFetchResponse fetchResponse) 
            throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(request.getObjectType().toString());

            // Create header row with column names
            List<ColumnMapping> columnMappings = createHeaderRow(workbook, sheet, fetchResponse);

            // Add data rows
            addDataRows(workbook, sheet, fetchResponse, columnMappings);

            // Auto-size columns
            for (ColumnMapping mapping : columnMappings) {
                sheet.autoSizeColumn(mapping.getColumnIndex());
            }

            // Write to file
            workbook.write(outputStream);
        }
    }
    
    /**
     * Handle export errors
     */
    private void handleExportError(Exception e, Path extractingFilePath, Path failedFilePath) {
        log.error("Error processing export in background", e);
        try {
            Files.writeString(failedFilePath, "Export failed: " + e.getMessage(), StandardCharsets.UTF_8);
            // Clean up extracting file if it exists
            if (Files.exists(extractingFilePath)) {
                Files.delete(extractingFilePath);
            }
        } catch (IOException ioe) {
            log.error("Error writing failure file", ioe);
        }
    }

    /**
     * Create a header row in the Excel sheet
     */
    private List<ColumnMapping> createHeaderRow(Workbook workbook, Sheet sheet, TableFetchResponse response) {
        List<ColumnMapping> columnMappings = new ArrayList<>();

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create header row
        Row headerRow = sheet.createRow(0);

        // First collect all column keys from response
        if (response.getFieldNameMap() != null) {
            int colIndex = 0;
            for (Map.Entry<String, ColumnInfo> entry : response.getFieldNameMap()
                    .entrySet()) {

                // Skip complex object fields
                if ("OBJECT".equals(entry.getValue().getFieldType())) {
                    continue;
                }

                String columnKey = entry.getKey();
                String displayName = entry.getValue().getFieldName();

                // Create header cell
                Cell cell = headerRow.createCell(colIndex);
                cell.setCellValue(displayName);
                cell.setCellStyle(headerStyle);

                // Add to ordered column mappings list
                columnMappings.add(new ColumnMapping(displayName, columnKey, colIndex));
                colIndex++;
            }
        }
        return columnMappings;
    }

    /**
     * Add data rows to the Excel sheet
     */
    private void addDataRows(Workbook workbook, Sheet sheet,
            TableFetchResponse response, List<ColumnMapping> columnMappings) {

        // Create date style for date fields
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

        // Add data rows
        int rowIndex = 1;
        if (response.getRows() != null) {
            for (TableRow tableRow : response.getRows()) {
                Row row = sheet.createRow(rowIndex++);

                // Add cells for each column in the same order as the headers
                for (ColumnMapping mapping : columnMappings) {
                    Cell cell = row.createCell(mapping.getColumnIndex());

                    // Get value from tableRow data using the field key
                    Object value = tableRow.getData().get(mapping.getFieldKey());

                    setCellValue(cell, value);
                }
            }
        }
    }

    /**
     * Set cell value based on value type
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
