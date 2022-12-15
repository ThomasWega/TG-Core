package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.config.DefaultConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.debug.DebugColors;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final Core core;

    public ConfigManager(Core core) {
        this.core = core;
    }

    // creates a specified file (config)
    private void createConfig(String filePath, String fileName) {
        File file;
        if (filePath.isEmpty()) {
            file = new File(core.getDataFolder(), fileName);
        } else {
            file = new File(core.getDataFolder() + File.separator + filePath, fileName);
        }
        boolean isFileCreated = file.exists();
        if (!isFileCreated) {
            core.getLogger().info(DebugColors.YELLOW + file.getName() + " not found, creating...");
            try {
                isFileCreated = file.createNewFile();
            } catch (IOException e) {
                core.getLogger().info(DebugColors.RED + "Couldn't create " + file.getName());
                throw new RuntimeException(e);
            }
            if (isFileCreated) {
                core.getLogger().info(DebugColors.GREEN + "Done creating " + file.getName());
            } else {
                core.getLogger().info(DebugColors.RED + "Couldn't create " + file.getName());
            }
        }
    }

    // creates the defaults for every config
    public void createConfigsDefaults() {

        // default config.yml
        DefaultConfig defaultConfig = new DefaultConfig(core);
        defaultConfig.create();

        // other configs
        MariaConfig mariaConfig = new MariaConfig(core);
        mariaConfig.mariaDefaults();
        AnnouncerConfig announcerConfig = new AnnouncerConfig(core);
        announcerConfig.announcerDefaults();
    }

    // creates the configs files
    public void createAllConfigFiles() {
        core.saveDefaultConfig();
        createConfig("", "spawn.yml");
        createConfig("", "announcer.yml");
        createConfig("", "mariadb.yml");
    }
}
