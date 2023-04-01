package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.player.data.config.PlayerDataType;
import net.trustgames.core.player.data.event.PlayerDataUpdateEvent;
import net.trustgames.core.player.data.level.PlayerLevel;
import net.trustgames.core.utils.LevelUtils;
import net.trustgames.database.HikariManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.data.PlayerDataDB.tableName;

/**
 * This class is used to fetch and update the player data database table
 */
public final class PlayerDataFetcher {

    private final Core core;
    private final HikariManager hikariManager;
    private PlayerDataType dataType;


    public PlayerDataFetcher(Core core, @NotNull PlayerDataType dataType) {
        this.core = core;
        this.dataType = dataType;
        this.hikariManager = core.getHikariManager();
    }

    /**
     * Get the player data Object from the column "label" that corresponds
     * to the given uuid. This whole operation is run async, and the result is saved
     * in the callback. If no result is found, int "0" is returned
     *
     * @param callback Callback where the result will be saved
     * @implNote Can't fetch player's UUID!
     * @see PlayerDataFetcher#fetchUUID(String, Consumer)
     */
    public void fetch(@NotNull UUID uuid, Consumer<@Nullable String> callback) {
        if (hikariManager.isDisabled()){
            callback.accept(null);
            return;
        }

        if (dataType == PlayerDataType.UUID) {
            throw new RuntimeException(this.getClass().getName() + " can't be used to retrieve UUID. " +
                    "Use the " + UUIDCache.class.getName() + " instead!");
        }

        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            if (dataType == PlayerDataType.LEVEL) {
                PlayerLevel playerLevel = new PlayerLevel(core, uuid);
                playerLevel.getLevel(level -> callback.accept(String.valueOf(level)));
                return;
            }

            String label = dataType.getColumnName();
            try (Connection connection = hikariManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT " + label + " FROM " + tableName + " WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        Object object = results.getObject(label);
                        callback.accept(object.toString());
                        return;
                    }
                    callback.accept(null);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Gets specifically only the Player's uuid by his name from the database.
     * This whole operation is run async, and the result is saved
     * in the callback. If no result is found, null is returned
     *
     * @param callback Callback where the result will be saved
     * @implNote Can't fetch anything other that Player's UUID!
     * @see PlayerDataFetcher#fetch(UUID, Consumer)
     */
    public void fetchUUID(@NotNull String playerName, Consumer<@Nullable UUID> callback) {
        if (hikariManager.isDisabled()){
            callback.accept(null);
            return;
        }
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = hikariManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM " + tableName + " WHERE name = ?")) {
                statement.setString(1, playerName);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    String stringUuid = result.getString("uuid");
                    try {
                        callback.accept(UUID.fromString(stringUuid));
                        return;
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("INVALID UUID FOR: " + stringUuid, e);
                    }
                }
                callback.accept(null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Updates the given DataType column with the given object
     * It uses transactions, to ensure that other updates don't interfere
     * with each other.
     * Data is also updated in the redis cache.
     *
     * @param object Object to update the DataType with
     */
    public void update(@NotNull UUID uuid, @NotNull Object object) {
        if (hikariManager.isDisabled()) return;

        // if XP, the level also needs to be recalculated and updated
        if (dataType == PlayerDataType.XP) {
            int level = LevelUtils.getLevelByXp(Integer.parseInt(object.toString()));
            PlayerDataCache levelCache = new PlayerDataCache(core, uuid, PlayerDataType.LEVEL);
            levelCache.update(String.valueOf(level));
        }

        // if LEVEL, it needs to be recalculated to XP and updated in the cache
        if (dataType == PlayerDataType.LEVEL) {
            PlayerDataCache levelCache = new PlayerDataCache(core, uuid, PlayerDataType.LEVEL);
            levelCache.update(object.toString());
            object = LevelUtils.getThreshold(Integer.parseInt(object.toString()));
            dataType = PlayerDataType.XP;
        }

        String label = dataType.getColumnName();

        Connection connection = hikariManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + tableName + "(uuid, " + label + ") VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE " + label + " = VALUES(" + label + ")")) {
            statement.setString(1, uuid.toString());
            statement.setObject(2, object);
            connection.setAutoCommit(false); // disable auto-commit mode to start a transaction
            statement.executeUpdate();
            connection.commit();

            // update the cache
            PlayerDataCache playerDataCache = new PlayerDataCache(core, uuid, dataType);
            playerDataCache.update(object.toString());

            // call the event from the main thread
            core.getServer().getScheduler().runTask(core, () ->
                    Bukkit.getPluginManager().callEvent(new PlayerDataUpdateEvent(uuid)));
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}