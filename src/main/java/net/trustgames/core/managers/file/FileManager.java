package net.trustgames.core.managers.file;

import net.trustgames.core.Core;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * Handles the creation of custom configs
 */
public final class FileManager {

    private static final Logger logger = Core.LOGGER;

    /**
     * creates a specified files and its paths
     *
     * @param plugin Instance of the plugin to get the path
     * @param files  One or more files to create
     */
    public static void createFile(@NotNull Plugin plugin, @NotNull File... files) {
        for (File file : files) {
            if (!file.exists()) {
                logger.warning(file.getName() + " not found, creating...");
                if (file.getParentFile().mkdirs()) {
                    logger.info("Created directory paths for " + file.getName());
                }
                plugin.saveResource(file.getName(), false);
            }
        }
    }
}
