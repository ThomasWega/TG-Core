package net.trustgames.core.managers;

import net.trustgames.core.debug.DebugColors;
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
                Bukkit.getLogger().info(DebugColors.YELLOW + "Data folder not found, creating...");
                if (folder.mkdirs()) {
                    Bukkit.getLogger().info(DebugColors.GREEN + "Done creating data folder");
                } else {
                    Bukkit.getLogger().info(DebugColors.RED + "Failed creating data folder");
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(DebugColors.RED + "Couldn't create data folder");
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
            Bukkit.getLogger().info(DebugColors.YELLOW + "Folder " + folder.getPath() + " not found, creating...");
            if (folder.mkdir()) {
                Bukkit.getLogger().info(DebugColors.GREEN + "Done creating folder " + folder.getPath());
            } else {
                Bukkit.getLogger().info(DebugColors.RED + "Failed creating folder " + folder.getPath());
            }
        }
    }
}

