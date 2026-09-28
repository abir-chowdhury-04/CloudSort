package com.abir_2307055.cloudsort.cloudsort.model;

public class FileItem {

    private final String name;
    private final String extension;
    private final String category;
    private final String reason;
    private final long sizeBytes;

    public FileItem(String name, String extension, String category, String reason, long sizeBytes) {
        this.name = name;
        this.extension = extension;
        this.category = category;
        this.reason = reason;
        this.sizeBytes = sizeBytes;
    }

    public String getName() { return name; }
    public String getExtension() { return extension; }
    public String getCategory() { return category; }
    public String getReason() { return reason; }
    public long getSizeBytes() { return sizeBytes; }

    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        double kb = sizeBytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        return String.format("%.2f GB", mb / 1024.0);
    }
}