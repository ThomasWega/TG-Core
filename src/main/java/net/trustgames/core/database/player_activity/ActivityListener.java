package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class ActivityListener implements Listener {

    private final Core core;

    public ActivityListener(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MariaDB mariaDB = new MariaDB(core);
        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

        if (mariaDB.isMySQLEnabled()) {
            Player player = event.getPlayer();
            // adds +1 to the killed player death stats
            PlayerActivity playerActivity;
            try {
                playerActivity = getPlayerActivityFromDatabase(player);

                if (playerActivity == null) {
                    core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 1");
                    playerActivity = new PlayerActivity(1, player.getUniqueId().toString(), null, new Timestamp(Instant.now().toEpochMilli()));
                    try {
                        core.getLogger().info(DebugColors.YELLOW_BACKGROUND);
                        playerActivityDB.createPlayerActivity(playerActivity);
                    } catch (SQLException e) {
                        core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when creating the Player Statistics in the database");
                        throw new RuntimeException(e);
                    }
                }
                else{
                    playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));
                    playerActivity.setAction("JOIN PORT " + Bukkit.getServer().getPort());
                    // updates the stats
                    core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 2");
                //    playerActivityDB.updatePlayerActivity(playerActivity);
                    playerActivityDB.createPlayerActivity(playerActivity);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // logout date
    }

    // gets the player stats from the database
    private PlayerActivity getPlayerActivityFromDatabase(Player player) throws SQLException {

        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

        // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
        PlayerActivity playerActivity = playerActivityDB.findPlayerActivityByUUID(player.getUniqueId().toString());

        // try to create the stats for the player
        return playerActivity;
     }
}
