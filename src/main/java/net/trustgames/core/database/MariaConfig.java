package net.trustgames.core.database;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Sets the config defaults for MariaDB (mariadb.yml)
 */
public class MariaConfig {

    private final Core core;

    public MariaConfig(Core core) {
        this.core = core;
    }

    /**
     * adds the defaults to the mariadb.yml file
     */
    public void createDefaults() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getMariaFile());

        // config defaults
        config.addDefault("mariadb.enable", false);
        config.addDefault("mariadb.user", "user");
        config.addDefault("mariadb.password", "passwd");
        config.addDefault("mariadb.ip", "127.0.0.1");
        config.addDefault("mariadb.port", "3306");
        config.addDefault("mariadb.database", "database");
        config.addDefault("delay.database-table-creation", 60L);
        try {
            config.options().copyDefaults(true);
            config.save(getMariaFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getMariaFile() {
        return new File(core.getDataFolder(), "mariadb.yml");
    }
}
