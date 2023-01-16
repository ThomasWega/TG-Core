package net.trustgames.core.managers;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

/**
 * Handles the creation of custom configs
 */
public class ConfigManager {

    /**
     * creates a specified file (config)
     *
     * @param file The file to create
     */
    public static void createConfig(File file) {
        boolean isFileCreated = file.exists();
        if (!isFileCreated) {
            Bukkit.getLogger().warning(file.getName() + " not found, creating...");
            try {
                isFileCreated = file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't create " + file.getName());
                throw new RuntimeException(e);
            }
            if (isFileCreated) {
                Bukkit.getLogger().warning("Done creating " + file.getName());
            } else {
                Bukkit.getLogger().severe("Couldn't create " + file.getName());
            }
        }
    }
}
