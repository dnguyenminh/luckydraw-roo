package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a file uploaded to the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadFile {

    /**
     * The name of the uploaded file
     */
    private String fileName;

    /**
     * The content of the file as byte array
     */
    private byte[] fileContent;
}
