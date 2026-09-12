package com.abir_2307055.cloudsort.cloudsort.model;

public class FileItem {

    private final String name;
    private final String extension;

    public FileItem(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }
}