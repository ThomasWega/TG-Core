package net.trustgames.core.tablist;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class TablistConfig {

    private final Core core;

    public TablistConfig(Core core) {
        this.core = core;
    }

    public void createDefaults(){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getTablistFile());

        config.addDefault("tablist.prefix.default", "%luckperms_prefix% &7%player_displayname%");
        config.addDefault("tablist.prefix.vip", "%luckperms_prefix% &f%player_displayname%");
        config.addDefault("tablist.prefix.vip+", "%luckperms_prefix% &f%player_displayname%");
        config.addDefault("tablist.prefix.trust", "%luckperms_prefix% &f%player_displayname%");
        config.addDefault("tablist.prefix.trust+", "%luckperms_prefix% &e%player_displayname%");

        try {
            config.options().copyDefaults(true);
            config.save(getTablistFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getTablistFile() {
        return new File(core.getDataFolder(), "tablist.yml");
    }
}
