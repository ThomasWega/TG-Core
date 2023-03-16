package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import net.trustgames.core.managers.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.activity.PlayerActivityDB.tableName;

public final class PlayerActivityFetcher {

    private final Core core;
    private final DatabaseManager databaseManager;

    public PlayerActivityFetcher(Core core) {
        this.core = core;
        this.databaseManager = core.getDatabaseManager();
    }

    /**
     * Gets the player's activity by his uuid and returns the result
     * as new list of Activities, which is saved in the callback. This
     * whole operation is run async.
     *
     * @param uuid     UUID of Player to get the activity for
     * @param callback Callback where the result will be saved
     */
    public void fetchByUUID(UUID uuid, Consumer<PlayerActivity> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ? ORDER BY id DESC")) {
                statement.setString(1, uuid.toString());
                try (ResultSet results = statement.executeQuery()) {
                    PlayerActivity activity = new PlayerActivity(uuid, new ArrayList<>());
                    while (results.next()) {
                        long id = results.getLong("id");
                        String ip = results.getString("ip");
                        String action = results.getString("action");
                        Timestamp time = results.getTimestamp("time");
                        activity.add(id, ip, action, time);
                    }
                    callback.accept(activity);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Returns only one Activity with the matching id in async callback
     *
     * @param id       Given ID
     * @param callback Callback where the result will be saved
     */
    public void fetchByID(long id, Consumer<PlayerActivity.Activity> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {

            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id = ?")) {
                statement.setString(1, String.valueOf(id));
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        long resultId = results.getLong("id");
                        UUID uuid = UUID.fromString(results.getString("uuid"));
                        String ip = results.getString("ip");
                        String action = results.getString("action");
                        Timestamp time = results.getTimestamp("time");
                        PlayerActivity.Activity activity = new PlayerActivity.Activity(resultId, uuid, ip, action, time);
                        callback.accept(activity);
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
     * @param activity Only one Activity with the corresponding data
     */
    public void insertNew(PlayerActivity.Activity activity) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "INSERT INTO " + tableName + " (uuid, ip, action, time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, activity.getUuid().toString());
                statement.setString(2, activity.getIp());
                statement.setString(3, activity.getAction());
                statement.setTimestamp(4, activity.getTime());
                statement.executeUpdate();
                try (ResultSet result = statement.getGeneratedKeys()) {
                    if (result.next()) {
                        long id = result.getLong(1);
                        activity.setId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting player activity into database", e);
        }
    }
}
