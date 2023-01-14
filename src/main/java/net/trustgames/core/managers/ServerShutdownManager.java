package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Handles what should happen when the server shutdowns.
 * This method DOESN'T move players to the next online server!
 */
public class ServerShutdownManager {

    private final Core core;

    public ServerShutdownManager(Core core) {
        this.core = core;
    }

    /**
     * Kick all the online players and log their activity
     */
    public void kickPlayers() {
        ActivityListener activityListener = new ActivityListener(core);

        if (core.getMariaDB().isMySQLDisabled()){
            Bukkit.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Not logging player activities. MariaDB is turned OFF!");
        }
        else{
            Bukkit.getLogger().info(DebugColors.BLUE + "Trying to log players activities...");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Component.text(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(core.getConfig().getString("messages.server-restart")))));
            if (!core.getMariaDB().isMySQLDisabled()){
                activityListener.onServerShutdown(player);
            }
        }
        if (!core.getMariaDB().isMySQLDisabled()){
            Bukkit.getLogger().info(DebugColors.CYAN + "Online players activities successfully saved to the database");
        }
    }
}
