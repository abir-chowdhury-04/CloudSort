package com.abir_2307055.cloudsort.cloudsort.service;

import java.util.Map;

public class Categorizer {

    private final Map<String, String> extensionToCategory;

    public Categorizer(RuleSource ruleSource) {
        this.extensionToCategory = ruleSource.loadRules();
    }

    public static Categorizer withDefaultRules() {
        return new Categorizer(new HardcodedRuleSource());
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