package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/*
This class is used to handle the database table player_activity
including methods, functions etc.
 */
public class PlayerActivityDB {

    private final Core core;

    public PlayerActivityDB(Core core) {
        this.core = core;
    }

    /*
     use external method from MariaDB class
     with specified SQL statement to create a new table
     (is run async)
    */
    public void initializePlayerActivityTable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String preparedStatement = "CREATE TABLE IF NOT EXISTS player_activity(id BIGINT unsigned primary key AUTO_INCREMENT, uuid varchar(36), ip varchar(15), action TINYTEXT, time DATETIME)";
                core.getMariaDB().initializeTable("player_activity", preparedStatement);
            }

        }.runTaskAsynchronously(core);
    }

    /*
     finds the current saved player's activity by his uuid
     by showing only rows with player's uuid and ordering it by largest id
     then it limits the results to only one. That should be the last row created
     and the one that should be set with the new stats (ip, time, action, ...)
    */
    public PlayerActivity findPlayerActivityByUUID(String uuid) {

        try {
            // gets all columns of all rows that include player's uuid. It orders them by largest id and limits the results to only one row
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("SELECT * FROM player_activity WHERE uuid = ? ORDER BY id DESC LIMIT 1")) {
                // sets the '?' to given uuid
                statement.setString(1, uuid);
                // gets the results, which in this case should be only one result
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {

                        // saves the results as variables
                        String ip = results.getString("ip");
                        String action = results.getString("action");
                        Timestamp time = results.getTimestamp("time");

                        // passes the variables to new PlayerActivity
                        return new PlayerActivity(uuid, ip, action, time);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /*
    creates a new row with the new player activity.
    values from playerActivity are set for each index
    (is run async)
     */
    public void createPlayerActivity(PlayerActivity playerActivity, boolean runAsync) throws SQLException {

        if (runAsync) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // inserts new player's activity to table
                    try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("INSERT INTO player_activity(uuid, ip, action, time) VALUES (?, ?, ?, ?)")) {
                        // replaces the '?' with variables
                        statement.setString(1, playerActivity.getUuid());
                        statement.setString(2, playerActivity.getIp());
                        statement.setString(3, playerActivity.getAction());
                        statement.setTimestamp(4, playerActivity.getTime());

                        statement.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.runTaskAsynchronously(core);
        } else {
            // inserts new player's activity to table
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("INSERT INTO player_activity(uuid, ip, action, time) VALUES (?, ?, ?, ?)")) {
                // replaces the '?' with variables
                statement.setString(1, playerActivity.getUuid());
                statement.setString(2, playerActivity.getIp());
                statement.setString(3, playerActivity.getAction());
                statement.setTimestamp(4, playerActivity.getTime());

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
