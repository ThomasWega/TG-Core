package net.trustgames.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class DefaultConfig {

    public static void create(@NotNull FileConfiguration defaultConfig) {
        defaultConfig.addDefault("settings.messages.server-restart", "&eServer is restarting...");
    }
}
