package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
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
import java.util.Objects;

/*
This class is used for listeners for table player_activity
 */
public class ActivityListener implements Listener {

    private final Core core;

    public ActivityListener(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        writeActivity(player, "JOIN PORT " + Bukkit.getServer().getPort());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // check if mysql is enabled in the mariadb.yml
        Player player = event.getPlayer();
        writeActivity(player, "QUIT PORT " + Bukkit.getServer().getPort());
    }

    /*
    Gets the player's last activity by using external method from PlayerActivityDB
    to find player's last activity by his uuid. If the activity is null, meaning the player
    probably doesn't have any activities saved in the table yet, it creates one with specified values
     */
    private PlayerActivity getPlayerActivityFromDatabase(Player player) throws SQLException {

        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

        // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
        PlayerActivity playerActivity = playerActivityDB.findPlayerActivityByUUID(player.getUniqueId().toString());

        if (playerActivity == null) {
            try {
                playerActivity = new PlayerActivity(player.getUniqueId().toString(), Objects.requireNonNull(player.getAddress()).getHostString(), "FIRST JOIN PORT " + Bukkit.getServer().getPort(), new Timestamp(Instant.now().toEpochMilli()));
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

    private void writeActivity(Player player, String action) {
        // check if mysql is enabled in the mariadb.yml
        if (core.getMariaDB().isMySQLEnabled()) {
            PlayerActivity playerActivity;
            try {
                playerActivity = getPlayerActivityFromDatabase(player);
                if (playerActivity != null) {
                    PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);


                    // set the action and time
                    playerActivity.setIp(Objects.requireNonNull(player.getAddress()).getHostString());
                    playerActivity.setAction(action);
                    playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));

                    // create new stat
                    playerActivityDB.createPlayerActivity(playerActivity);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
