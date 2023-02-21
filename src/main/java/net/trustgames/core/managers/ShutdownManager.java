package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.config.ServerConfig;
import net.trustgames.core.player.activity.PlayerActivityHandler;
import net.trustgames.core.logger.CoreLogger;
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
        PlayerActivityHandler activityListener = new PlayerActivityHandler(core);

        if (core.getMariaDB().isMySQLDisabled()){
            CoreLogger.LOGGER.warning("Not logging player activities. MariaDB is turned OFF");
        }
        else{
            CoreLogger.LOGGER.info("Trying to log players activities...");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kick(ServerConfig.RESTART.getText());
                activityListener.onShutdown(EntityCache.getUUID(player));
            }
            CoreLogger.LOGGER.finest("Online players activities successfully saved to the database");
        }
    }
}
