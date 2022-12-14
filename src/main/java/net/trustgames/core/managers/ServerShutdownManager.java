package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ServerShutdownManager {

    private final Core core;

    public ServerShutdownManager(Core core) {
        this.core = core;
    }

    public void kickPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Component.text(ChatColor.translateAlternateColorCodes('&', String.join("\n", core.getConfig().getString("settings.messages.server-restart")))));
        }
    }
}
