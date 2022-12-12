package net.trustgames.core.database.player_stats;

import net.trustgames.core.Core;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        PlayerStatsDB playerStatsDB = new PlayerStatsDB(core);

        if (mariaDB.isMySQLEnabled()) {
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
                    playerStatsDB.updatePlayerStats(killerStats);
                }
                // adds +1 to the killed player death stats
                PlayerStats playerStats = getPlayerStatsFromDatabase(player);
                playerStats.setDeaths(playerStats.getDeaths() + 1);

                // updates the stats
                playerStatsDB.updatePlayerStats(playerStats);
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when saving kills stats of " + Objects.requireNonNull(killer).getName() + " and deaths stats of" + player.getName() + "to player_stats table!");
                throw new RuntimeException(e);
            }
        }
    }

    // gets the player stats from the database
    private PlayerStats getPlayerStatsFromDatabase(Player player) throws SQLException {

        PlayerStatsDB playerStatsDB = new PlayerStatsDB(core);

        // find the playerStats for the player using method findPlayerStatsByUUID in MariaDB class
        PlayerStats playerStats = playerStatsDB.findPlayerStatsByUUID(player.getUniqueId().toString());

        // if the player doesn't have any stats yet, create ones with 0
        if (playerStats == null) {
            playerStats = new PlayerStats(player.getUniqueId().toString(), 0, 0, 0, 0, 0.0, 0.0, 0.0, new Timestamp(Instant.now().toEpochMilli()));

            // try to create the stats for the player
            try {
                playerStatsDB.createPlayerStats(playerStats);
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when creating the Player Statistics in the database");
                throw new RuntimeException(e);
            }
        }
        return playerStats;
    }
}
