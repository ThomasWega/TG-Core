package net.trustgames.core.player.activity;

import net.trustgames.core.Core;

import java.sql.*;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is used to handle the database table for player activity
 * including its methods, functions etc.
 */
public final class PlayerActivityDB {

    private static final String tableName = "player_activity";

    private final Core core;

    public PlayerActivityDB(Core core) {
        this.core = core;
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        String statement = "CREATE TABLE IF NOT EXISTS " + tableName + "(id BIGINT unsigned primary key AUTO_INCREMENT, uuid VARCHAR(36), ip VARCHAR(15), action TINYTEXT, time DATETIME)";
        core.getMariaDB().initializeTable(tableName, statement);
    }

    public void fetchByUuid(UUID uuid, Consumer<PlayerActivity> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ? ORDER BY id DESC LIMIT 1")) {
                statement.setString(1, uuid.toString());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        String ip = results.getString("ip");
                        String action = results.getString("action");
                        Timestamp time = results.getTimestamp("time");
                        PlayerActivity activity = new PlayerActivity(uuid, ip, action, time);
                        callback.accept(activity);
                    } else {
                        callback.accept(null);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * creates a new row with the new player activity.
     * values from playerActivity are set for each index
     * (is run async)
     *
     * @param playerActivity Player activity instance
     */
    public void add(PlayerActivity playerActivity) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + "(uuid, ip, action, time) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, playerActivity.getUuid().toString());
                statement.setString(2, playerActivity.getIp());
                statement.setString(3, playerActivity.getAction());
                statement.setTimestamp(4, playerActivity.getTime());

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
