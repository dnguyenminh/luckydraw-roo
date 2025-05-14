package vn.com.fecredit.app.service.impl;

import vn.com.fecredit.app.service.ChunkedUploadService;
import vn.com.fecredit.app.service.dto.ObjectType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ChunkedUploadService for handling chunked file uploads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkedUploadServiceImpl implements ChunkedUploadService {

    @Value("${app.temp-dir:${java.io.tmpdir}}")
    private String tempDirPath;
    
    private String getChunkedUploadsDir() {
        return tempDirPath + File.separator + "chunked-uploads";
    }

    @Override
    public void saveChunk(
            ObjectType objectType, 
            String sessionId, 
            String fileName, 
            int chunkIndex, 
            int totalChunks, 
            byte[] data) throws IOException {
        
        // Create session directory if it doesn't exist
        String sessionDir = getSessionDir(objectType, sessionId);
        Path dirPath = Paths.get(sessionDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // Save the chunk file
        String chunkPath = getChunkPath(objectType, sessionId, chunkIndex);
        try (FileOutputStream fos = new FileOutputStream(chunkPath)) {
            fos.write(data);
        }
        
        // Save metadata for the session
        saveMetadata(objectType, sessionId, fileName, chunkIndex, totalChunks);
        
        log.info("Saved chunk {} of {} for session {} ({})", 
                 chunkIndex + 1, totalChunks, sessionId, fileName);
    }
    
    @Override
    public Path combineChunksToFile(ObjectType objectType, String sessionId, int totalChunks) throws IOException {
        log.info("Combining {} chunks for session {}", totalChunks, sessionId);
        
        // Check if all chunks are available
        for (int i = 0; i < totalChunks; i++) {
            String chunkPath = getChunkPath(objectType, sessionId, i);
            if (!Files.exists(Paths.get(chunkPath))) {
                throw new IOException("Chunk " + i + " is missing for session " + sessionId);
            }
        }
        
        // Create the combined file
        String sessionDir = getSessionDir(objectType, sessionId);
        String metadataPath = sessionDir + File.separator + "metadata.json";
        String metadataContent = new String(Files.readAllBytes(Paths.get(metadataPath)));
        String fileName = extractFileNameFromMetadata(metadataContent);
        
        Path combinedFilePath = Paths.get(sessionDir, fileName);
        
        // Use FileChannel for efficient file combining without loading into memory
        try (FileOutputStream outputStream = new FileOutputStream(combinedFilePath.toFile())) {
            try (FileChannel outputChannel = outputStream.getChannel()) {
                for (int i = 0; i < totalChunks; i++) {
                    File chunkFile = new File(getChunkPath(objectType, sessionId, i));
                    try (FileInputStream inputStream = new FileInputStream(chunkFile);
                         FileChannel inputChannel = inputStream.getChannel()) {
                        // Transfer from input channel to output channel
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    }
                }
            }
        }
        
        log.info("Combined file created at: {}", combinedFilePath);
        return combinedFilePath;
    }
    
    /**
     * Extract filename from metadata JSON
     */
    private String extractFileNameFromMetadata(String metadataJson) {
        // Simple extraction without full JSON parsing for efficiency
        int fileNameIndex = metadataJson.indexOf("\"fileName\":\"");
        if (fileNameIndex >= 0) {
            int startIndex = fileNameIndex + 12; // Length of "fileName":"
            int endIndex = metadataJson.indexOf("\"", startIndex);
            if (endIndex > startIndex) {
                return metadataJson.substring(startIndex, endIndex);
            }
        }
        // Fallback to a default name if extraction fails
        return "uploaded_file_" + System.currentTimeMillis();
    }
    
    @Override
    public void cleanupChunks(String sessionId) {
        String chunkedUploadsDir = getChunkedUploadsDir();
        String sessionDir = chunkedUploadsDir + File.separator + sessionId;
        try {
            File directory = new File(sessionDir);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    Arrays.stream(files).forEach(File::delete);
                }
                directory.delete();
                log.info("Cleaned up temporary files for session {}", sessionId);
            }
        } catch (Exception e) {
            log.warn("Failed to clean up session directory {}: {}", sessionId, e.getMessage());
        }
    }
    
    /**
     * Retrieves session metadata for a given upload session
     * 
     * @param sessionId The upload session identifier
     * @return Map containing session metadata with keys:
     *         - fileName: String - The original file name
     *         - totalChunks: Integer - Total number of chunks
     * @throws IllegalArgumentException If session information cannot be found
     */
    @Override
    public Map<String, Object> getSessionInfo(String sessionId) {
        log.debug("Retrieving session info for session ID: {}", sessionId);
        
        // Find the session directory by searching in all object type subdirectories
        String chunkedUploadsDir = getChunkedUploadsDir();
        File uploadsDir = new File(chunkedUploadsDir);
        
        if (!uploadsDir.exists()) {
            throw new IllegalArgumentException("No upload sessions found - uploads directory doesn't exist");
        }
        
        // We need to search through each object type directory
        File[] objectTypeDirs = uploadsDir.listFiles(File::isDirectory);
        if (objectTypeDirs == null || objectTypeDirs.length == 0) {
            throw new IllegalArgumentException("No upload session found with ID: " + sessionId);
        }
        
        for (File objectTypeDir : objectTypeDirs) {
            File sessionDir = new File(objectTypeDir, sessionId);
            if (sessionDir.exists()) {
                // Found the session directory, read metadata file
                File metadataFile = new File(sessionDir, "metadata.json");
                if (metadataFile.exists()) {
                    try {
                        String metadataJson = new String(Files.readAllBytes(metadataFile.toPath()));
                        return parseMetadataJson(metadataJson);
                    } catch (IOException e) {
                        log.error("Failed to read metadata file for session {}: {}", sessionId, e.getMessage());
                        throw new IllegalArgumentException("Failed to read session metadata: " + e.getMessage());
                    }
                }
            }
        }
        
        throw new IllegalArgumentException("No upload session found with ID: " + sessionId);
    }

    /**
     * Parse the metadata JSON string into a Map
     */
    private Map<String, Object> parseMetadataJson(String metadataJson) {
        Map<String, Object> result = new HashMap<>();
        
        // Extract fileName using regex
        Pattern fileNamePattern = Pattern.compile("\"fileName\":\"([^\"]+)\"");
        Matcher fileNameMatcher = fileNamePattern.matcher(metadataJson);
        if (fileNameMatcher.find()) {
            result.put("fileName", fileNameMatcher.group(1));
        }
        
        // Extract totalChunks using regex
        Pattern totalChunksPattern = Pattern.compile("\"totalChunks\":(\\d+)");
        Matcher totalChunksMatcher = totalChunksPattern.matcher(metadataJson);
        if (totalChunksMatcher.find()) {
            result.put("totalChunks", Integer.parseInt(totalChunksMatcher.group(1)));
        }
        
        // If we didn't find the required fields, throw an exception
        if (!result.containsKey("fileName") || !result.containsKey("totalChunks")) {
            throw new IllegalArgumentException("Invalid metadata format: missing required fields");
        }
        
        return result;
    }
    
    private String getSessionDir(ObjectType objectType, String sessionId) {
        return getChunkedUploadsDir() + File.separator + objectType.name() + File.separator + sessionId;
    }
    
    private String getChunkPath(ObjectType objectType, String sessionId, int chunkIndex) {
        return getSessionDir(objectType, sessionId) + File.separator + "chunk_" + chunkIndex;
    }
    
    private void saveMetadata(
            ObjectType objectType, 
            String sessionId, 
            String fileName, 
            int chunkIndex, 
            int totalChunks) throws IOException {
        
        String metadataPath = getSessionDir(objectType, sessionId) + File.separator + "metadata.json";
        String metadataJson = String.format(
            "{\"fileName\":\"%s\",\"objectType\":\"%s\",\"totalChunks\":%d,\"lastChunkReceived\":%d,\"timestamp\":%d}",
            fileName, objectType.name(), totalChunks, chunkIndex, System.currentTimeMillis()
        );
        
        Files.write(Paths.get(metadataPath), metadataJson.getBytes());
    }
}
