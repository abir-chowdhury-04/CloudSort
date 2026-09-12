package com.abir_2307055.cloudsort.cloudsort.model;

public class FileItem {

    private final String name;
    private final String extension;
    private final String category;
    private final String reason;

    public FileItem(String name, String extension, String category, String reason) {
        this.name = name;
        this.extension = extension;
        this.category = category;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getCategory() {
        return category;
    }

    public String getReason() {
        return reason;
    }
}