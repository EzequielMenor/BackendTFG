package com.eze.gymanalytics.api.dto;

import java.util.List;

public class ImportResultDTO {

    private int successCount;
    private int failedCount;
    private List<String> failedRows;

    public ImportResultDTO(int successCount, int failedCount, List<String> failedRows) {
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.failedRows = failedRows;
    }

    public int getSuccessCount() { return successCount; }
    public int getFailedCount() { return failedCount; }
    public List<String> getFailedRows() { return failedRows; }
}
