package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        writeActivity(player, "JOIN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // check if mysql is enabled in the mariadb.yml
        Player player = event.getPlayer();
        writeActivity(player, "QUIT SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", true);
    }

    public void onServerShutdown(Player player) {
        writeActivity(player, "QUIT SHUTDOWN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", false);
    }

    /*
    Gets the player's last activity by using external method from PlayerActivityDB
    to find player's last activity by his uuid. If the activity is null, meaning the player
    probably doesn't have any activities saved in the table yet, it creates one with specified values
     */
    private PlayerActivity getPlayerActivityFromDatabase(Player player, boolean runAsync) {

        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

        // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
        PlayerActivity playerActivity = playerActivityDB.findPlayerActivityByUUID(player.getUniqueId().toString());

        if (playerActivity == null) {
            playerActivity = new PlayerActivity(player.getUniqueId().toString(), Objects.requireNonNull(player.getAddress()).getHostString(), "FIRST JOIN PORT " + Bukkit.getServer().getPort(), new Timestamp(Instant.now().toEpochMilli()));
            playerActivityDB.createPlayerActivity(playerActivity, runAsync);
            return null;
        } else {
            return playerActivity;
        }
    }

    /*
    writes the values for the newly created player activity to the table.
    Assigns a new player activity by getting the player's activity from
    the database and setting the values accordingly. Then it creates the full row.
     */
    private void writeActivity(Player player, String action, boolean runAsync) {
        // check if mysql is enabled in the mariadb.yml
        if (core.getMariaDB().isMySQLDisabled()) return;

        PlayerActivity playerActivity;
        playerActivity = getPlayerActivityFromDatabase(player, runAsync);
        if (playerActivity != null) {
            PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);


            // set the action and time
            playerActivity.setIp(Objects.requireNonNull(player.getAddress()).getHostString());
            playerActivity.setAction(action);
            playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));

            // create new stat
            playerActivityDB.createPlayerActivity(playerActivity, runAsync);
        }
    }
}
