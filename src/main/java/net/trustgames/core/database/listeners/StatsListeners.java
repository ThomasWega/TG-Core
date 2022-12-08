package net.trustgames.core.database.listeners;

import net.trustgames.core.Core;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.models.PlayerStats;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

public class StatsListeners implements Listener {

    private final Core core;

    public StatsListeners(Core core) {
        this.core = core;
    }

    // this one listener updates both the kills and deaths counter
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MariaDB mariaDB = new MariaDB(core);
        if (mariaDB.isMySQLEnabled()){
            Player player = event.getPlayer();
            Player killer = event.getPlayer().getKiller();
            // do only if the killer is a Player
            try {
                if (killer != null) {
                    // adds +1 to the killer kill stats
                    System.out.println(event.getPlayer().getKiller());
                    PlayerStats killerStats = getPlayerStatsFromDatabase(killer);
                    killerStats.setKills(killerStats.getKills() + 1);

                    // updates the killer stats
                    core.getMariaDB().updatePlayerStats(killerStats);
                }
                // adds +1 to the killed player death stats
                PlayerStats playerStats = getPlayerStatsFromDatabase(player);
                playerStats.setDeaths(playerStats.getDeaths() + 1);

                // updates the stats
                core.getMariaDB().updatePlayerStats(playerStats);
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when saving kills stats of " + Objects.requireNonNull(killer).getName() + " and deaths stats of" + player.getName() + "to player_stats table!");
                throw new RuntimeException(e);
            }
        }
    }

    // log the last player join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        MariaDB mariaDB = new MariaDB(core);
        if (mariaDB.isMySQLEnabled()){
            //    playerStats.setLogout(logout);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        MariaDB mariaDB = new MariaDB(core);
        if (mariaDB.isMySQLEnabled()){
            Player player = event.getPlayer();
            try {
                PlayerStats playerStats = getPlayerStatsFromDatabase(player);
            //    playerStats.setLogout(logout);
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when saving last join of " + player.getName() + " to player_stats table!");
                throw new RuntimeException(e);
            }
        }
    }

    // gets the player stats from the database
    private PlayerStats getPlayerStatsFromDatabase(Player player) throws SQLException {
            // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
            PlayerStats playerStats = core.getMariaDB().findPlayerStatsByUUID(player.getUniqueId().toString());

            // if the player doesn't have any stats yet, create ones with 0
            if (playerStats == null) {
                playerStats = new PlayerStats(player.getUniqueId().toString(), 0, 0, 0, 0, 0.0, 0.0, 0.0, new Timestamp(Instant.now().toEpochMilli()));

                // try to create the stats for the player
                try {
                    this.core.getMariaDB().createPlayerStats(playerStats);
                } catch (SQLException e) {
                    core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when creating the Player Statistics in the database");
                    throw new RuntimeException(e);
                }
            }
        return playerStats;
    }
}
