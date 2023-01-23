package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

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

        String message1 = "<newline><yellow>1111</yellow><newline><hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK<newline><yellow>1111</yellow><newline>";
        String message2 = "<newline><yellow>2222</yellow><newline><hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://store.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK<newline><yellow>2222</yellow><newline>";

        config.addDefault("announcer.messages.message1", message1);
        config.addDefault("announcer.messages.message2", message2);
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
