package com.abir_2307055.cloudsort.cloudsort.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {

    private static final String SETTINGS_FILE_NAME = "cloudsort-settings.properties";
    private static final String DARK_MODE_KEY = "darkModeEnabled";

    public boolean loadDarkModePreference() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(SETTINGS_FILE_NAME)) {
            properties.load(input);
        } catch (IOException e) {
            return false;
        }
        return Boolean.parseBoolean(properties.getProperty(DARK_MODE_KEY, "false"));
    }

    public void saveDarkModePreference(boolean darkModeEnabled) {
        Properties properties = new Properties();
        properties.setProperty(DARK_MODE_KEY, String.valueOf(darkModeEnabled));

        try (FileOutputStream output = new FileOutputStream(SETTINGS_FILE_NAME)) {
            properties.store(output, "CloudSort user settings");
        } catch (IOException e) {
            // If saving fails, the app should keep running - the preference
            // just won't persist to next launch. Not a critical failure.
        }
    }
}