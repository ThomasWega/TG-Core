package net.trustgames.core.managers;

import org.bukkit.Bukkit;

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
            Bukkit.getLogger().warning("Folder " + folder.getPath() + " not found, creating...");
            if (folder.mkdir()) {
                Bukkit.getLogger().finest("Done creating folder " + folder.getPath());
            } else {
                Bukkit.getLogger().severe("Failed creating folder " + folder.getPath());
            }
        }
    }
}

