package com.abir_2307055.cloudsort.cloudsort.model;

public class ScanSettings {
    private boolean includeHiddenFiles = false;
    private boolean skipEmptyFiles = false;
    private boolean caseSensitiveMatching = false;
    private String sortOrder = "Name";
    private String conflictStrategy = "Auto-rename";
    private int maxHistoryResults = 50;
    private String confirmationPassphrase = "";

    public boolean isIncludeHiddenFiles() { return includeHiddenFiles; }
    public void setIncludeHiddenFiles(boolean v) { this.includeHiddenFiles = v; }

    public boolean isSkipEmptyFiles() { return skipEmptyFiles; }
    public void setSkipEmptyFiles(boolean v) { this.skipEmptyFiles = v; }

    public boolean isCaseSensitiveMatching() { return caseSensitiveMatching; }
    public void setCaseSensitiveMatching(boolean v) { this.caseSensitiveMatching = v; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String v) { this.sortOrder = v; }

    public String getConflictStrategy() { return conflictStrategy; }
    public void setConflictStrategy(String v) { this.conflictStrategy = v; }

    public int getMaxHistoryResults() { return maxHistoryResults; }
    public void setMaxHistoryResults(int v) { this.maxHistoryResults = v; }

    public String getConfirmationPassphrase() { return confirmationPassphrase; }
    public void setConfirmationPassphrase(String v) { this.confirmationPassphrase = v; }
}