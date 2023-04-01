package net.trustgames.core.managers;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static net.trustgames.core.Core.LOGGER;

/**
 * Handles the creation of custom configs
 */
public final class FileManager {

    /**
     * creates a specified files and its paths
     *
     * @param files   One or more files to create
     * @param plugin Instance of the plugin to get the path
     */
    public static void createFile(@NotNull Plugin plugin, @NotNull File... files) {
        for (File file : files) {
            if (!file.exists()) {
                LOGGER.warning(file.getName() + " not found, creating...");
                if (file.getParentFile().mkdirs()) {
                    LOGGER.info("Created directory paths for " + file.getName());
                }
                plugin.saveResource(file.getName(), false);
            }
        }
    }
}
