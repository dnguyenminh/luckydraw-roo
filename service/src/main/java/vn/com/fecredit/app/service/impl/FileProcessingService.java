package vn.com.fecredit.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Service for file processing operations (import/export)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileProcessingService {

    /**
     * Process a file import for the given object type with chunked upload support
     */
    public <T extends AbstractPersistableEntity<?>> TableActionResponse processImportData(
            TableActionRequest request,
            EntityManager entityManager,
            RepositoryFactory repositoryFactory,
            TableDataServiceImpl tableDataService,
            EntityMapperService entityMapperService
    ) {
        try {
            Path filePath = null;
            String fileName = null;

            // Handle import from file path (chunked upload)
            if (request.getFilePath() != null && !request.getFilePath().isEmpty()) {
                filePath = Paths.get(request.getFilePath());
                fileName = request.getFileName();
                log.info("Processing import from file path: {}", filePath);

                if (!Files.exists(filePath)) {
                    return TableActionResponse.error(request, "Import file not found at path: " + filePath);
                }
            }
            // Fallback to upload file content if available
            else if (request.getUploadFile() != null &&
                    request.getUploadFile().getFileContent() != null &&
                    request.getUploadFile().getFileContent().length > 0) {

                // For backward compatibility, create a temp file for small uploads
                fileName = request.getUploadFile().getFileName();
                byte[] fileData = request.getUploadFile().getFileContent();

                // Save small upload to a temporary file
                Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "import-uploads");
                Files.createDirectories(tempDir);
                filePath = tempDir.resolve(fileName);
                Files.write(filePath, fileData);

                log.info("Saved small upload to temporary file: {}", filePath);
            } else {
                return TableActionResponse.error(request, "Import request must include file path or content");
            }

            // Process the import file using streaming approaches
            try {
                // Get the entity class
                Class<T> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

                // Get column mapping
                TableFetchRequest emptyRequest = new TableFetchRequest();
                emptyRequest.setObjectType(request.getObjectType());
                emptyRequest.setPage(0);
                emptyRequest.setSize(1);

                TableFetchResponse metadataResponse = tableDataService.fetchData(emptyRequest);
                Map<String, Object> columnMapping = new HashMap<>();

                if (metadataResponse.getFieldNameMap() != null) {
                    metadataResponse.getFieldNameMap().forEach((key, info) -> {
                        columnMapping.put(info.getFieldName().toLowerCase(), key);
                    });
                }

                // Use arrays to track counts so they can be modified from lambda
                final int[] counts = new int[] {0, 0}; // [totalRecords, successCount]
                List<String> errors = new ArrayList<>();

                // Use streaming to process records without loading all into memory
                processExcelFileStreaming(filePath, columnMapping, (record) -> {
                    counts[0]++; // Increment totalRecords
                    try {
                        T entity = entityMapperService.createEntityFromMap(record, entityClass);
                        entity = entityMapperService.saveEntity(entity, repositoryFactory);
                        counts[1]++; // Increment successCount

                        // Flush periodically to avoid memory buildup
                        if (counts[1] % 100 == 0) {
                            entityManager.flush();
                            entityManager.clear();
                        }

                        return true; // Continue processing
                    } catch (Exception e) {
                        errors.add("Error importing record: " + e.getMessage() + ", Data: " + record);
                        return errors.size() < 100; // Stop if too many errors
                    }
                });

                // Create response
                String message = String.format(
                        "Import completed. Imported %d records successfully. %d records failed.",
                        counts[1], errors.size());

                // Add first error if any
                if (!errors.isEmpty()) {
                    message += " First error: " + errors.get(0);
                }

                TableRow resultRow = new TableRow();
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("totalRecords", counts[0]);
                resultData.put("successCount", counts[1]);
                resultData.put("errorCount", errors.size());
                resultData.put("errors", errors);
                resultRow.setData(resultData);

                // Create response with success() factory method instead of constructor
                TableActionResponse response;
                if (errors.isEmpty()) {
                    response = TableActionResponse.success(request, message, resultRow);
                } else {
                    response = TableActionResponse.error(request, message);
                    response.setData(resultRow);
                }
                
                return response;
            } finally {
                // Clean up temporary file when done (if needed)
                if (request.getUploadFile() != null) {
                    // Only delete if it was created from uploadFile
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        log.warn("Failed to delete temporary file: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing IMPORT action", e);
            return TableActionResponse.error(request, "Failed to import data: " + e.getMessage());
        }
    }

    /**
     * Interface for processing each record as it's read from Excel
     */
    @FunctionalInterface
    public interface ExcelRecordProcessor {
        /**
         * Process a single record from the Excel file
         * @param record The record data
         * @return true to continue processing, false to stop
         */
        boolean process(Map<String, Object> record);
    }

    /**
     * Parse Excel file by streaming records one at a time using SAX-based approach
     */
    private void processExcelFileStreaming(Path filePath, Map<String, Object> columnMapping,
            ExcelRecordProcessor processor) {

        // Determine file type based on extension
        String fileName = filePath.getFileName().toString().toLowerCase();

        try {
            if (fileName.endsWith(".xlsx")) {
                processXlsxStreaming(filePath, columnMapping, processor);
            } else if (fileName.endsWith(".xls")) {
                processXlsxStreaming(filePath, columnMapping, processor);
            } else if (fileName.endsWith(".csv")) {
                processCsvStreaming(filePath, columnMapping, processor);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + fileName);
            }
        } catch (Exception e) {
            log.error("Error parsing file: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing import file: " + e.getMessage(), e);
        }
    }

    /**
     * Process XLSX files using Apache POI's event model
     */
    private void processXlsxStreaming(Path filePath, Map<String, Object> columnMapping,
            ExcelRecordProcessor processor) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(filePath.toFile())) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();

            // Get the first sheet only
            Iterator<InputStream> sheets = reader.getSheetsData();
            if (!sheets.hasNext()) {
                throw new IllegalArgumentException("No sheets found in Excel file");
            }

            try (InputStream sheetStream = sheets.next()) {
                // Use SAXParserFactory instead of deprecated XMLReaderFactory
                javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                org.xml.sax.XMLReader parser = factory.newSAXParser().getXMLReader();

                // Create content handler to process rows as they are read
                XSSFSheetHandler handler = new XSSFSheetHandler(sst, columnMapping, processor);

                parser.setContentHandler(handler);
                parser.parse(new InputSource(sheetStream));
            }
        }
    }

    /**
     * Process CSV files by reading line by line
     */
    private void processCsvStreaming(Path filePath, Map<String, Object> columnMapping,
            ExcelRecordProcessor processor) throws IOException {
        // Use CSV parser from Apache Commons CSV with the newer builder pattern
        try (Reader reader = Files.newBufferedReader(filePath)) {
            // Use the builder pattern with get() instead of deprecated build()
            CSVParser csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader);

            Map<String, Integer> headerMap = csvParser.getHeaderMap();

            // Map CSV headers to field names
            Map<Integer, String> fieldMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                String headerName = entry.getKey().toLowerCase();
                Object fieldName = columnMapping.get(headerName);
                if (fieldName != null) {
                    fieldMap.put(entry.getValue(), fieldName.toString());
                }
            }

            // Process records one by one
            for (CSVRecord record : csvParser) {
                Map<String, Object> data = new HashMap<>();

                for (Map.Entry<Integer, String> entry : fieldMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();

                    if (columnIndex < record.size()) {
                        data.put(fieldName, record.get(columnIndex));
                    }
                }

                if (!data.isEmpty() && !processor.process(data)) {
                    break; // Stop processing if processor returns false
                }
            }
        }
    }
}
