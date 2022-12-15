package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;

public class ChatAnnouncer {

    private final Core core;

    public ChatAnnouncer(Core core) {
        this.core = core;
    }

    AnnouncerConfig announcerConfig;

    public void announceMessages() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(announcerConfig.getAnnouncerFile());

        BukkitScheduler scheduler = core.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(core, new Runnable() {
            int i = 1;

            /*
             runs once every x seconds (specified in announcer.yml) and loops through each online player
             it sends every player the same message. After all players have the message, it increases
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
