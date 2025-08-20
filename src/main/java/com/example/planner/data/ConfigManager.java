// src/main/java/com/example/planner/data/ConfigManager.java
package com.example.planner.data;

import com.example.planner.model.UserSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    // Saves to <project-root>/data/settings.json
    private static final Path DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("settings.json");

    private static final ObjectMapper MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public static boolean settingsExists() {
        return Files.exists(FILE);
    }

    public static void save(UserSettings settings) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), settings);
    }

    public static UserSettings load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), UserSettings.class);
    }

    public static Path settingsPath() {
        return FILE;
    }
}
