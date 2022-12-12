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

        // check if mysql is enabled in the mariadb.yml
        if (mariaDB.isMySQLEnabled()) {
            Player player = event.getPlayer();
            PlayerActivity playerActivity;
            try {
                playerActivity = getPlayerActivityFromDatabase(player);
                if (playerActivity != null) {
                    PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);


                    // set the action and time
                    playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));
                    playerActivity.setAction("JOIN PORT " + Bukkit.getServer().getPort());

                    // create new stat
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

        // TODO move the null check here

        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

        // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
        PlayerActivity playerActivity = playerActivityDB.findPlayerActivityByUUID(player.getUniqueId().toString());

        if (playerActivity == null) {
            try {
                playerActivity = new PlayerActivity(player.getUniqueId().toString(), "FIRST JOIN", new Timestamp(Instant.now().toEpochMilli()));
                playerActivityDB.createPlayerActivity(playerActivity);
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when creating info in player_activity table");
                throw new RuntimeException(e);
            }
            return null;
        } else {
            return playerActivity;
        }
    }
}
