package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record AnnouncerConfig(Core core) {

    // create the config defaults for announcer.yml
    public void announcerDefaults() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(getAnnouncerFile());

        config.addDefault("announcer.time", 120);

        List<String> message1 = new ArrayList<>();
        message1.add("&r");
        message1.add("&c1");
        message1.add("&c11");
        message1.add("&8111");
        message1.add("&r");
        config.addDefault("announcer.messages.message1", message1);

        List<String> message2 = new ArrayList<>();
        message2.add("&r");
        message2.add("&c2");
        message2.add("&c22");
        message2.add("&8222");
        message2.add("&r");
        config.addDefault("announcer.messages.message2", message2);
        try {
            config.options().copyDefaults(true);
            config.save(getAnnouncerFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // used to retrieve the announcer.yml file
    public File getAnnouncerFile() {
        return new File(core.getDataFolder(), "announcer.yml");
    }
}
