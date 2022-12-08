package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;

import java.io.File;

public record FolderManager(Core core) {

    // Data folder = /plugins/Core folder
    public void createDataFolder() {
        // check if the folder exists, in case it doesn't, create it.
        try {
            if (!core.getDataFolder().exists()) {
                core.getLogger().info(DebugColors.YELLOW + "Data folder not found, creating...");
                if (core.getDataFolder().mkdirs()) {
                    core.getLogger().info(DebugColors.GREEN + "Done creating data folder");
                } else {
                    core.getLogger().info(DebugColors.RED + "Failed creating data folder");
                }
            }
        } catch (Exception e) {
            core.getLogger().info(DebugColors.RED + "Couldn't create data folder");
            throw new RuntimeException(e);
        }
    }

    // creates the specified folder
    public void createFolder(String folderName) {

        File file = new File(core.getDataFolder() + File.separator + folderName);

        // check if the /plugins/Core exists, if not it runs the method createDataFolder to create it
        if (!core.getDataFolder().exists()) {
            createDataFolder();
        }
        // check if the folder exists, in case it doesn't, it creates it.
        if (!file.exists()) {
            core.getLogger().info(DebugColors.YELLOW + "Folder " + file.getPath() + " not found, creating...");
            if (file.mkdir()) {
                core.getLogger().info(DebugColors.GREEN + "Done creating folder " + file.getPath());
            } else {
                core.getLogger().info(DebugColors.RED + "Failed creating folder " + file.getPath());
            }
        }
    }

    // create the specific folders
    public void createAllFolders() {
        createFolder("data");
    }
}

