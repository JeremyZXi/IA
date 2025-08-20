package com.example.planner.data;

import com.example.planner.model.MasterList;
import com.example.planner.model.TaskList;
import com.example.planner.model.UserSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UserDataManager {
    private static final Path DIR  = Path.of(System.getProperty("user.dir"), "data");
    private static final Path FILE = DIR.resolve("data.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static boolean dataExists() {
        return Files.exists(FILE);
    }

    public static void save(MasterList taskLists) throws Exception {
        if (!Files.exists(DIR)){
            Files.createDirectories(DIR);
        }
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), taskLists);
    }
    public static MasterList load() throws Exception {
        return MAPPER.readValue(FILE.toFile(), MasterList.class);
    }

    public static Path userDataPath() { return FILE; }


}
