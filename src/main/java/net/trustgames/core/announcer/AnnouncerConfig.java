package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Sets the config defaults for Announcer (announcer.yml)
 */
public class AnnouncerConfig {

    private final Core core;

    public AnnouncerConfig(Core core) {
        this.core = core;
    }

    /**
     * create the config defaults for announcer.yml
     */
    public void createDefaults() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(getAnnouncerFile());

        config.addDefault("announcer.time", 120);

        config.addDefault("announcer.messages.message1", List.of(
                "<newline>",
                "<yellow>1111</yellow>",
                "<hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK",
                "<yellow>1111</yellow>",
                "<newline>"
        ));
        config.addDefault("announcer.messages.message2", List.of(
                "<newline>",
                "<yellow>2222</yellow>",
                "<hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://store.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK",
                "<yellow>2222</yellow>",
                "<newline>"
        ));
        try {
            config.options().copyDefaults(true);
            config.save(getAnnouncerFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getAnnouncerFile() {
        return new File(core.getDataFolder(), "announcer.yml");
    }
}
