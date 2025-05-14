package vn.com.fecredit.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.TableActionResponse;

@Service
@Slf4j
public class ImportProgressTracker {

    public enum Status {
        PENDING, 
        VALIDATING, 
        IMPORTING, 
        COMPLETED, 
        FAILED
    }

    // Store job status information
    private final Map<String, Map<String, Object>> jobStatuses = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Create a new job entry
     */
    public void createJob(String jobId, String objectType) {
        Map<String, Object> jobStatus = new HashMap<>();
        jobStatus.put("status", Status.PENDING.name());
        jobStatus.put("validationProgress", 0);
        jobStatus.put("importProgress", 0);
        jobStatus.put("objectType", objectType);
        jobStatus.put("message", "Job created");
        jobStatus.put("errors", new ArrayList<Map<String, Object>>());
        jobStatus.put("createdAt", System.currentTimeMillis());
        jobStatuses.put(jobId, jobStatus);
    }

    /**
     * Get the current status of a job
     */
    public Map<String, Object> getJobStatus(String jobId) {
        return jobStatuses.get(jobId);
    }

    /**
     * Update the status of a job
     */
    public void updateStatus(String jobId, Status status) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null) {
            jobStatus.put("status", status.name());
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Update the validation progress
     */
    public void updateValidationProgress(String jobId, int progress) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null) {
            int clampedProgress = Math.min(100, Math.max(0, progress));
            jobStatus.put("validationProgress", clampedProgress);
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Update the import progress
     */
    public void updateImportProgress(String jobId, int progress) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null) {
            int clampedProgress = Math.min(100, Math.max(0, progress));
            jobStatus.put("importProgress", clampedProgress);
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Update job message
     */
    public void updateMessage(String jobId, String message) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null) {
            jobStatus.put("message", message);
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Add an error to the job
     */
    public void addError(String jobId, int row, String errorMessage) {
        addError(jobId, row, errorMessage, null, null);
    }

    /**
     * Add a detailed error to the job
     */
    public void addError(String jobId, int row, String errorMessage, String field, String value) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errors = (List<Map<String, Object>>) jobStatus.get("errors");
            if (errors == null) {
                errors = new ArrayList<>();
                jobStatus.put("errors", errors);
            }
            
            Map<String, Object> error = new HashMap<>();
            error.put("row", row);
            error.put("message", errorMessage);
            
            if (field != null) {
                error.put("field", field);
            }
            
            if (value != null) {
                error.put("value", value);
            }
            
            errors.add(error);
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Update statistics for the import job
     */
    public void updateStatistics(String jobId, Map<String, Object> statistics) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus != null && statistics != null) {
            jobStatus.put("statistics", statistics);
            jobStatus.put("updatedAt", System.currentTimeMillis());
        }
    }

    /**
     * Extract statistics from import result
     */
    public Map<String, Object> extractImportStatistics(TableActionResponse result) {
        Map<String, Object> statistics = new HashMap<>();
        
        if (result.getData() != null && result.getData().getData() != null) {
            Map<String, Object> data = result.getData().getData();
            
            // Extract total records, success and error counts if available
            if (data.containsKey("totalRecords")) {
                statistics.put("totalRecords", data.get("totalRecords"));
            }
            
            if (data.containsKey("successCount")) {
                statistics.put("successCount", data.get("successCount"));
            }
            
            if (data.containsKey("errorCount")) {
                statistics.put("errorCount", data.get("errorCount"));
            }
            
            // Calculate success percentage if we have both fields
            if (statistics.containsKey("totalRecords") && statistics.containsKey("successCount")) {
                int total = Integer.parseInt(statistics.get("totalRecords").toString());
                int success = Integer.parseInt(statistics.get("successCount").toString());
                
                if (total > 0) {
                    double successRate = (double) success / total * 100.0;
                    statistics.put("successRate", successRate);
                }
            }
        }
        
        return statistics;
    }

    /**
     * Extract errors from import result
     */
    public void extractImportErrors(String jobId, TableActionResponse result) {
        if (result != null && result.getErrors() != null) {
            for (Object error : result.getErrors()) {
                if (error instanceof String) {
                    addError(jobId, 0, (String) error);
                } else if (error instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> errorMap = (Map<String, Object>) error;
                    int row = errorMap.containsKey("row") ? 
                        Integer.parseInt(errorMap.get("row").toString()) : 0;
                    String message = errorMap.getOrDefault("message", "Unknown error").toString();
                    String field = errorMap.containsKey("field") ? 
                        errorMap.get("field").toString() : null;
                    String value = errorMap.containsKey("value") ? 
                        errorMap.get("value").toString() : null;
                    
                    addError(jobId, row, message, field, value);
                }
            }
        }
    }

    /**
     * Start an asynchronous process
     */
    public void startAsyncProcess(String jobId, Runnable process) {
        new Thread(() -> {
            try {
                process.run();
            } catch (Exception e) {
                log.error("Error in async process for job {}: {}", jobId, e.getMessage(), e);
                updateStatus(jobId, Status.FAILED);
                updateMessage(jobId, "Process failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Schedule cleanup for completed or failed jobs
     * Keeps jobs in memory for 1 hour after completion
     */
    public void scheduleJobCleanup(String jobId) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus == null) return;
        
        String status = (String) jobStatus.get("status");
        if (Status.COMPLETED.name().equals(status) || Status.FAILED.name().equals(status)) {
            cleanupService.schedule(() -> {
                log.debug("Cleaning up job status for completed job: {}", jobId);
                jobStatuses.remove(jobId);
            }, 60, TimeUnit.MINUTES);
        }
    }
    
    /**
     * For backwards compatibility - simulate progress
     * @deprecated Use real progress tracking instead
     */
    @Deprecated
    public void simulateProgress(String jobId, String progressField, int targetPercentage) {
        Map<String, Object> jobStatus = jobStatuses.get(jobId);
        if (jobStatus == null) return;
        
        int current = jobStatus.containsKey(progressField) ? 
            Integer.parseInt(jobStatus.get(progressField).toString()) : 0;
        
        final int target = Math.min(targetPercentage, 100);
        final int startValue = current;
        
        // Only simulate if we haven't already reached the target
        if (current < target) {
            // Use steps to simulate realistic progress
            Thread progressThread = new Thread(() -> {
                try {
                    int steps = 10;
                    int increment = (target - startValue) / steps;
                    for (int i = 1; i <= steps; i++) {
                        int progress = startValue + (increment * i);
                        
                        // Get fresh status in case it changed
                        Map<String, Object> status = jobStatuses.get(jobId);
                        if (status == null) break;
                        
                        // Update progress
                        status.put(progressField, progress);
                        Thread.sleep(200);
                    }
                    
                    // Ensure we reach the target
                    Map<String, Object> status = jobStatuses.get(jobId);
                    if (status != null) {
                        status.put(progressField, target);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            progressThread.setDaemon(true);
            progressThread.start();
        }
    }
}
