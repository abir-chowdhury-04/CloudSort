package com.abir_2307055.cloudsort.cloudsort.service;

import java.util.HashMap;
import java.util.Map;

public class HardcodedRuleSource implements RuleSource {

    @Override
    public Map<String, String> loadRules() {
        Map<String, String> rules = new HashMap<>();
        rules.put("pdf", "Documents");
        rules.put("docx", "Documents");
        rules.put("txt", "Documents");
        rules.put("jpg", "Images");
        rules.put("jpeg", "Images");
        rules.put("png", "Images");
        return rules;
    }
}