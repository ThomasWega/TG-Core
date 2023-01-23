package net.trustgames.core.announcer;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.Core;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Chat messages which are announced to all online players
 * on the server.
 */
public class ChatAnnouncer {

    private final Core core;

    public ChatAnnouncer(Core core) {
        this.core = core;
    }

    /**
     * Announce a set of messages every x seconds for all
     * the online players on the server. The messages can be configured
     * in the announcer.yml config
     */
    public void announceMessages() {
        AnnouncerConfig announcerConfig = new AnnouncerConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(announcerConfig.getAnnouncerFile());

        new BukkitRunnable() {
            int i = 1;

            /**
             After all players have the message, it increases
             the int i by one, to move on to the next message in the announcer.yml. When it sent all the messages
             it sets the int i back to one and starts all over again.
            */
            @Override
            public void run() {

                String path = "announcer.messages.message";
                core.getServer().broadcast(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(
                        config.getString(path + i),
                        "String on path " + path + " wasn't found in config!")));

                ConfigurationSection section = config.getConfigurationSection("announcer.messages");
                if (i == Objects.requireNonNull(section,
                        "Configuration section " + section + " wasn't found in config!")
                        .getKeys(false).size()) {
                    i = 1;
                    return;
                }
                i++;
            }
        }.runTaskTimerAsynchronously(core, 300, config.getLong("announcer.time") * 20);
    }
}
