package net.trustgames.core.managers;

import net.trustgames.core.logger.CoreLogger;

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
        if (!file.exists()) {
            CoreLogger.LOGGER.warning(file.getName() + " not found, creating...");
            try {
                if (file.createNewFile())
                    CoreLogger.LOGGER.finest("Done creating " + file.getName());
            } catch (IOException e) {
                CoreLogger.LOGGER.severe("Couldn't create " + file.getName());
                throw new RuntimeException(e);
            }
        }
    }
}
