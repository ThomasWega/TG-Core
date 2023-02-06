package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.settings.server.CoreServer;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles what should happen when the server shutdowns.
 * This method DOESN'T move players to the next online server!
 */
public class ShutdownManager {

    private final Core core;

    public ShutdownManager(Core core) {
        this.core = core;
    }

    /**
     * Kick all the online players and log their activity to database
     */
    public void kickPlayers() {
        ActivityListener activityListener = new ActivityListener(core);

        if (core.getMariaDB().isMySQLDisabled()){
            Bukkit.getLogger().warning("Not logging player activities. MariaDB is turned OFF");
        }
        else{
            Bukkit.getLogger().info("Trying to log players activities...");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kick(ColorUtils.color(CoreServer.RESTART));
                activityListener.onServerShutdown(player);
            }
            Bukkit.getLogger().finest("Online players activities successfully saved to the database");
        }
    }
}
