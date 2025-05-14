package vn.com.fecredit.app.service;

import java.util.function.BiFunction;
import vn.com.fecredit.app.service.dto.ImportError;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Service interface for processing import files
 */
public interface ImportFileProcessor {
    
    /**
     * Count the number of rows in an Excel file
     * 
     * @param filePath Path to the Excel file
     * @return Number of data rows (excluding header)
     * @throws Exception If an error occurs reading the file
     */
    int countExcelRows(String filePath) throws Exception;
    
    /**
     * Validate an import file
     * 
     * @param filePath Path to the file
     * @param objectType Type of objects being imported
     * @param validationCallback Callback that receives row number and error (if any)
     *                          Returns boolean indicating whether to continue validation
     * @throws Exception If an error occurs processing the file
     */
    void validateFile(String filePath, ObjectType objectType, 
                     BiFunction<Integer, ImportError, Boolean> validationCallback) throws Exception;
}
