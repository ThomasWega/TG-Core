package net.trustgames.core.config;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.FileConfiguration;

public class DefaultConfig {

    private final Core core;

    public DefaultConfig(Core core) {
        this.core = core;
    }

    public void create() {
        FileConfiguration defaultConfig = core.getConfig();
        defaultConfig.addDefault("settings.server-type", "LOBBY");
        defaultConfig.addDefault("settings.messages.server-restart", "&eServer is restarting...");
        core.getConfig().options().copyDefaults(true);
        core.saveConfig();
    }
}
