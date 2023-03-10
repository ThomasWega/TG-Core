package net.trustgames.core.managers;

import net.trustgames.core.logger.CoreLogger;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Handles the creation of custom configs
 */
public final class FileManager {

    /**
     * creates a specified file (config) and its path
     *
     * @param file The file to create
     * @param plugin Instance of the plugin to get the path
     */
    public static void createFile(Plugin plugin, File file) {
        if (!file.exists()){
            CoreLogger.LOGGER.warning(file.getName() + " not found, creating...");
            if (file.getParentFile().mkdirs()){
             CoreLogger.LOGGER.info("Created directory paths for " + file.getName());
            }
            plugin.saveResource(file.getName(), false);
            if (file.exists()){
                CoreLogger.LOGGER.warning("Done creating " + file.getName());
            } else {
                CoreLogger.LOGGER.severe("Couldn't create " + file.getName());
            }
        }
    }
}
