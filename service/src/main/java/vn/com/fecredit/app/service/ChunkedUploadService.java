package vn.com.fecredit.app.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Service for handling chunked file uploads
 */
public interface ChunkedUploadService {
    
    /**
     * Save a chunk of a file
     * 
     * @param objectType Object type associated with the file
     * @param sessionId Session identifier for this upload
     * @param fileName Original name of the file
     * @param chunkIndex Index of this chunk
     * @param totalChunks Total number of chunks expected
     * @param data Chunk data as byte array
     */
    void saveChunk(ObjectType objectType, String sessionId, String fileName, 
                  int chunkIndex, int totalChunks, byte[] data) throws IOException;
    
    /**
     * Combine all chunks into a single file
     * 
     * @param objectType Object type associated with the file
     * @param sessionId Session identifier for this upload
     * @param totalChunks Total number of chunks to combine
     * @return Path to the combined file
     */
    Path combineChunksToFile(ObjectType objectType, String sessionId, int totalChunks) throws IOException;
    
    /**
     * Clean up chunks after upload is complete
     * 
     * @param sessionId Session identifier for the upload
     */
    void cleanupChunks(String sessionId);
    
    /**
     * Retrieve session metadata for a given upload session
     * 
     * This method returns information about the upload session including
     * the original file name and total number of chunks.
     * 
     * @param sessionId The upload session identifier
     * @return Map containing session metadata with keys:
     *         - fileName: String - The original file name
     *         - totalChunks: Integer - Total number of chunks
     * @throws IllegalArgumentException If session information cannot be found
     */
    Map<String, Object> getSessionInfo(String sessionId);
}
