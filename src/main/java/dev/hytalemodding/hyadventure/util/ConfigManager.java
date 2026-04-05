package dev.hytalemodding.hyadventure.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.hytalemodding.hyadventure.models.AuthoredQuestData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private final Object saveLock = new Object();
    private AuthoredQuestData data;

    public ConfigManager(@Nonnull Path dataFolder) {
        try { Files.createDirectories(dataFolder); } catch (IOException ignored) {}
        this.dataFile = dataFolder.resolve("authored-quests.json");
        load();
    }

    @Nonnull
    public AuthoredQuestData getData() {
        return data;
    }

    public void load() {
        try {
            if (Files.exists(dataFile)) {
                String json = Files.readString(dataFile);
                data = gson.fromJson(json, AuthoredQuestData.class);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[HyAdventure] Failed to load authored quests: " + e.getMessage());
        }
        if (data == null) data = new AuthoredQuestData();
    }

    public void save() {
        synchronized (saveLock) {
            try {
                String json = gson.toJson(data);
                Path tmp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
                Files.writeString(tmp, json);
                try {
                    Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (Exception e) {
                    Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                LOGGER.atWarning().log("[HyAdventure] Failed to save authored quests: " + e.getMessage());
            }
        }
    }
}
