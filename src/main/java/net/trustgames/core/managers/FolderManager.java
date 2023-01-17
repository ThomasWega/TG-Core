package net.trustgames.core.managers;

import org.bukkit.Bukkit;

import java.io.File;

/**
 * Handles the creation of folders
 */
public class FolderManager {

    /**
     * Creation of the data folder.
     * Data folder = /plugins/Core folder
     *
     * @param folder Folder
     */
    public static void createDataFolder(File folder) {
        // check if the folder exists, in case it doesn't, create it.
        try {
            if (!folder.exists()) {
                Bukkit.getLogger().warning("Data folder not found, creating...");
                if (folder.mkdirs()) {
                    Bukkit.getLogger().finest( "Done creating data folder");
                } else {
                    Bukkit.getLogger().severe("Failed creating data folder");
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Couldn't create data folder");
            throw new RuntimeException(e);
        }
    }

    /** creates the specified folder
     *
     * @param folder Folder to create
     */
    public static void createFolder(File folder) {

        // check if the folder exists, in case it doesn't, it creates it
        // .
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

