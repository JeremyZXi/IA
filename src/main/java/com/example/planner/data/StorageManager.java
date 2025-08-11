package com.example.planner.data;

import com.example.planner.model.MasterList;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;

public class StorageManager {
    private static final Path DIR  = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("storage.json");
    private static final ObjectMapper MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule());
    public static boolean storageExists() {
        return Files.exists(FILE);
    }
    public static void save(MasterList todo) throws Exception {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), todo);
    }
    public static MasterList load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), MasterList.class);
    }
    public static Path storagePath() { return FILE; }

}
