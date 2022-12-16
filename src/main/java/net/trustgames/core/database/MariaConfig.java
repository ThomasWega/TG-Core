package net.trustgames.core.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MariaConfig {

    // adds the defaults to the mariadb.yml file
    public static void createDefaults() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getMariaFile());

        // config defaults
        config.addDefault("mariadb.enable", false);
        config.addDefault("mariadb.user", "user");
        config.addDefault("mariadb.password", "passwd");
        config.addDefault("mariadb.ip", "127.0.0.1");
        config.addDefault("mariadb.port", "3306");
        config.addDefault("mariadb.database", "database");
        try {
            config.options().copyDefaults(true);
            config.save(getMariaFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // used to retrieve the mariadb.yml file
    public static File getMariaFile() {
        return new File(Bukkit.getPluginsFolder() + File.separator + "Core", "mariadb.yml");
    }
}
