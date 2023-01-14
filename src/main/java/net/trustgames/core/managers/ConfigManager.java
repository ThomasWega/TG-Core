package net.trustgames.core.managers;

import net.trustgames.core.debug.DebugColors;
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
            Bukkit.getLogger().info(DebugColors.YELLOW + file.getName() + " not found, creating...");
            try {
                isFileCreated = file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().info(DebugColors.RED + "Couldn't create " + file.getName());
                throw new RuntimeException(e);
            }
            if (isFileCreated) {
                Bukkit.getLogger().info(DebugColors.GREEN + "Done creating " + file.getName());
            } else {
                Bukkit.getLogger().info(DebugColors.RED + "Couldn't create " + file.getName());
            }
        }
    }
}
