package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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

        core.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable() {
            int i = 1;

            /**
             After all players have the message, it increases
             the int i by one, to move on to the next message in the announcer.yml. When it sent all the messages
             it sets the int i back to one and starts all over again.
            */
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", config.getStringList("announcer.messages.message" + i))));
                }
                if (i == Objects.requireNonNull(config.getConfigurationSection("announcer.messages")).getKeys(false).size()) {
                    i = 1;
                } else {
                    i++;
                }
            }
        }, 0L, config.getLong("announcer.time") * 20);
    }
}
