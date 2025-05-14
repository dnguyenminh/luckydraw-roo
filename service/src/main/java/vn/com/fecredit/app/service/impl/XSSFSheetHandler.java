package vn.com.fecredit.app.service.impl;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import vn.com.fecredit.app.service.impl.FileProcessingService.ExcelRecordProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * SAX content handler for processing XLSX spreadsheets in a memory-efficient way
 */
public class XSSFSheetHandler extends DefaultHandler {
    private static final Logger logger = Logger.getLogger(XSSFSheetHandler.class.getName());
    
    private final SharedStringsTable sharedStringsTable;
    private final Map<String, Object> columnMapping;
    private final ExcelRecordProcessor processor;
    
    // State variables
    private boolean inValue = false;
    private boolean inRow = false;
    private StringBuilder value = new StringBuilder();
    private String currentColumn = null;
    private int currentRow = 0;
    private Map<String, String> headerMap = new HashMap<>();
    private List<String> headerPositions = new ArrayList<>();  // Stores header column references in order
    private List<String> headerValues = new ArrayList<>();     // Stores header values in order
    private Map<String, Object> currentRowData = null;
    private boolean isSharedString = false;
    
    public XSSFSheetHandler(
            SharedStringsTable sharedStringsTable,
            Map<String, Object> columnMapping, 
            ExcelRecordProcessor processor) {
        this.sharedStringsTable = sharedStringsTable;
        this.columnMapping = columnMapping;
        this.processor = processor;
        logger.info("Column mapping initialized with: " + columnMapping.keySet());
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        // Clear contents buffer
        value.setLength(0);
        
        // Handle row element
        if (qName.equals("row")) {
            int rowNum = Integer.parseInt(attributes.getValue("r"));
            if (rowNum > 1) { // Skip header row (row 1)
                inRow = true;
                currentRowData = new HashMap<>();
            } else if (rowNum == 1) { // Header row
                inRow = true;
            }
            currentRow = rowNum;
            return;
        }
        
        // Handle cell element
        if (qName.equals("c")) {
            // Get column reference
            String cellRef = attributes.getValue("r");
            if (cellRef != null && cellRef.length() > 0) {
                // Extract column letter(s) from the cell reference
                currentColumn = cellRef.replaceAll("\\d+", "");
            }
            
            // Check if this is a shared string (t="s")
            isSharedString = "s".equals(attributes.getValue("t"));
            
            inValue = true;
            return;
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("v") || qName.equals("t")) {
            // Process cell value
            String cellValue = value.toString();
            
            // Handle shared strings reference
            if (isSharedString && sharedStringsTable != null) {
                try {
                    int idx = Integer.parseInt(cellValue);
                    cellValue = sharedStringsTable.getItemAt(idx).getString();
                } catch (Exception e) {
                    logger.warning("Error parsing shared string: " + e.getMessage());
                }
            }
            
            if (inRow && currentColumn != null) {
                if (currentRow == 1) {
                    // For header row, store both position and value
                    headerPositions.add(currentColumn);
                    String headerText = cellValue.toLowerCase().trim();
                    headerValues.add(headerText);
                    headerMap.put(currentColumn, headerText);
                    
                    logger.info("Header at position " + currentColumn + ": '" + headerText + "'");
                    
                    // Validate if this header exists in column mapping
                    if (!columnMapping.containsKey(headerText)) {
                        logger.warning("No mapping for header: '" + headerText + "', available mappings: " + columnMapping.keySet());
                    }
                } else {
                    // Data row - add value to current row data
                    String headerName = headerMap.get(currentColumn);
                    if (headerName != null) {
                        // Look up the field name from columnMapping using the header name
                        Object fieldName = columnMapping.get(headerName);
                        if (fieldName != null) {
                            currentRowData.put(fieldName.toString(), cellValue);
                        } else {
                            // Try to find column index-based mapping if header name mapping fails
                            int columnIndex = headerPositions.indexOf(currentColumn);
                            if (columnIndex >= 0 && columnIndex < headerValues.size()) {
                                String headerValue = headerValues.get(columnIndex);
                                fieldName = columnMapping.get(headerValue);
                                if (fieldName != null) {
                                    currentRowData.put(fieldName.toString(), cellValue);
                                } else {
                                    logger.warning("No mapping found for header: '" + headerName + "' at position " + currentColumn);
                                }
                            } else {
                                logger.warning("No mapping found for column position: " + currentColumn);
                            }
                        }
                    }
                }
            }
            
            inValue = false;
            isSharedString = false;
        } else if (qName.equals("row")) {
            // End of row - process the data
            if (inRow && currentRow > 1 && !currentRowData.isEmpty()) {
                // Process the record
                boolean continueProcessing = processor.process(currentRowData);
                if (!continueProcessing) {
                    // Signal to stop processing more records
                    headerMap.clear();  // Clear headers to prevent more processing
                    headerPositions.clear();
                    headerValues.clear();
                }
            }
            
            inRow = false;
            currentRowData = null;
        }
    }
    
    /**
     * This method is called when character data is found within XML elements.
     * It accumulates text content between tags.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (inValue) {
            value.append(ch, start, length);
        }
    }
}
