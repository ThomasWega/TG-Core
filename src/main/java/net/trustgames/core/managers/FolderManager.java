package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;

import java.io.File;

public record FolderManager(Core core) {

    // Data folder = /plugins/Core folder
    public static void createDataFolder(File file) {
        // check if the folder exists, in case it doesn't, create it.
        try {
            if (!file.exists()) {
                Bukkit.getLogger().info(DebugColors.YELLOW + "Data folder not found, creating...");
                if (file.mkdirs()) {
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

    // creates the specified folder
    public static void createFolder(File file) {

        // check if the folder exists, in case it doesn't, it creates it.
        if (!file.exists()) {
            Bukkit.getLogger().info(DebugColors.YELLOW + "Folder " + file.getPath() + " not found, creating...");
            if (file.mkdir()) {
                Bukkit.getLogger().info(DebugColors.GREEN + "Done creating folder " + file.getPath());
            } else {
                Bukkit.getLogger().info(DebugColors.RED + "Failed creating folder " + file.getPath());
            }
        }
    }
}

