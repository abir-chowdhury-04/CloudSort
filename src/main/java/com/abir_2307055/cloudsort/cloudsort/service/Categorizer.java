package com.abir_2307055.cloudsort.cloudsort.service;

import java.util.HashMap;
import java.util.Map;

public class Categorizer {

    private final Map<String, String> extensionToCategory;

    public Categorizer(Map<String, String> extensionToCategory) {
        this.extensionToCategory = extensionToCategory;
    }

    public static Categorizer withDefaultRules() {
        Map<String, String> defaultRules = new HashMap<>();
        defaultRules.put("pdf", "Documents");
        defaultRules.put("docx", "Documents");
        defaultRules.put("txt", "Documents");
        defaultRules.put("jpg", "Images");
        defaultRules.put("jpeg", "Images");
        defaultRules.put("png", "Images");
        return new Categorizer(defaultRules);
    }

    public String getCategory(String extension) {
        String lowerExtension = extension.toLowerCase();
        return extensionToCategory.getOrDefault(lowerExtension, "Other");
    }

    public String getReason(String extension) {
        String lowerExtension = extension.toLowerCase();
        if (extensionToCategory.containsKey(lowerExtension)) {
            return "Matched ." + lowerExtension + " rule";
        }
        return "No matching rule - defaulted to Other";
    }
}