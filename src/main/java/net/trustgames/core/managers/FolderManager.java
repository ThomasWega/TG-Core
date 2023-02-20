package net.trustgames.core.managers;

import net.trustgames.core.logger.CoreLogger;

import java.io.File;

/**
 * Handles the creation of folders
 */
public class FolderManager {

    /** creates the specified folder
     *
     * @param folder Folder to create
     */
    public static void createFolder(File folder) {

        // check if the folder exists, in case it doesn't, it creates it
        if (!folder.exists()) {
            CoreLogger.LOGGER.warning("Folder " + folder.getPath() + " not found, creating...");
            if (folder.mkdir()) {
                CoreLogger.LOGGER.finest("Done creating folder " + folder.getPath());
            } else {
                CoreLogger.LOGGER.severe("Failed creating folder " + folder.getPath());
            }
        }
    }
}

