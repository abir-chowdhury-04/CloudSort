package com.abir_2307055.cloudsort.cloudsort.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class JsonRuleSource implements RuleSource {

    private static final String RULES_URL =
            "https://raw.githubusercontent.com/abir-chowdhury-04/CloudSort/main/cloudsort-rules.json";

    @Override
    public Map<String, String> loadRules() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RULES_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch rules, status: " + response.statusCode());
                return new HardcodedRuleSource().loadRules();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});

        } catch (IOException | InterruptedException e) {
            System.err.println("Could not fetch JSON rules, falling back to hardcoded rules: " + e.getMessage());
            return new HardcodedRuleSource().loadRules();
        }
    }
}