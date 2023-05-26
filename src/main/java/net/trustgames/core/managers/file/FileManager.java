package net.trustgames.core.managers.file;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Handles the creation of custom configs
 */
public final class FileManager {

    /**
     * creates a specified files and its paths
     *
     * @param plugin Instance of the plugin to get the path
     * @param files  One or more files to create
     */
    public static void createFile(@NotNull Plugin plugin, @NotNull File... files) {
        for (File file : files) {
            if (!file.exists()) {
                plugin.getComponentLogger().warn("{} not found, creating...", file.getName());
                if (file.getParentFile().mkdirs()) {
                    plugin.getComponentLogger().info("Created directory paths for {}", file.getName());
                }
                plugin.saveResource(file.getName(), false);
            }
        }
    }
}
