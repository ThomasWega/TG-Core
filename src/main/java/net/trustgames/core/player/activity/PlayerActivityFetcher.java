package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.activity.PlayerActivityDB.tableName;

public class PlayerActivityFetcher {

    private final Core core;

    public PlayerActivityFetcher(Core core) {
        this.core = core;
    }

    /**
     * Gets the latest player's activity by his uuid and returns the result
     * as new PlayerActivity instance, which is saved in the callback. This
     * whole operation is run async.
     *
     * @param uuid UUID of Player to get the activity for
     * @param callback Callback where the result will be saved
     */
    public void fetchByUUID(UUID uuid, Consumer<PlayerActivity> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getDatabaseManager().getConnection();
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
    public void insert(PlayerActivity playerActivity) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getDatabaseManager().getConnection();
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

    /**
     * Gets the player's last activity by using external method from PlayerActivityDB
     * to find player's last activity by his uuid. If the activity is null, meaning the player
     * probably doesn't have any activities saved in the table yet, it creates one with specified values.
     * Calls the provided callback function with the resulting PlayerActivity object.
     *
     * @param uuid     UUID of Player to write activity to
     * @param callback Callback function to be called with the resulting PlayerActivity object
     */
    private void get(UUID uuid, Consumer<PlayerActivity> callback) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            callback.accept(null);
            return;
        }

        fetchByUUID(uuid, playerActivity -> {
            if (playerActivity == null) {
                playerActivity = new PlayerActivity(uuid, Objects.requireNonNull(
                                player.getAddress(), "Player " + uuid + " IP address is null!")
                        .getHostString(), "FIRST JOIN SERVER " + Bukkit.getServer().getName(),
                        new Timestamp(Instant.now().toEpochMilli()));

                insert(playerActivity);
                callback.accept(null);
            } else {
                callback.accept(playerActivity);
            }
        });
    }

    /**
     * writes the values for the newly created player activity to the new PlayerActivity instance.
     * Then it creates the full row.
     *
     * @param uuid   UUID of Player to write activity to
     * @param action What actions to write
     */
    public void add(UUID uuid, String action) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;
        if (core.getDatabaseManager().isMySQLDisabled()) return;

        get(uuid, playerActivity -> {
            if (playerActivity != null) {
                playerActivity.setIp(Objects.requireNonNull(player.getAddress()).getHostString());

                playerActivity.setAction(action);
                playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));

                insert(playerActivity);
            }
        });
    }
}
