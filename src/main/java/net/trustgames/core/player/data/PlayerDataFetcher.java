package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.cache.DataCache;
import net.trustgames.core.config.cache.player_data.PlayerDataType;
import net.trustgames.core.player.data.additional.level.PlayerLevel;

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
    private final UUID uuid;
    private final DataCache dataCache;

    public PlayerDataFetcher(Core core, UUID uuid) {
        this.core = core;
        this.uuid = uuid;
        this.dataCache = new DataCache(core, uuid);
    }

    /**
     * Get the player data Object from the column "label" that corresponds
     * to the given uuid. This whole operation is run async, and the result is saved
     * in the callback. If no result is found, int "0" is returned
     *
     * @param playerDataType DataType which will be used to get the column name
     * @param callback       Callback where the result will be saved
     */
    public void fetch(PlayerDataType playerDataType, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () ->
                dataCache.fetch(playerDataType, data -> {
                    if (data != null) {
                        callback.accept(data);
                    } else {
                        if (playerDataType == PlayerDataType.LEVEL) {
                            PlayerLevel playerLevel = new PlayerLevel(core, uuid);
                            playerLevel.getLevel(level -> callback.accept(String.valueOf(level)));
                            return;
                        }

                        String label = playerDataType.getColumnName();
                        try (Connection connection = core.getMariaDB().getConnection();
                             PreparedStatement statement = connection.prepareStatement("SELECT " + label + " FROM " + tableName + " WHERE uuid = ?")) {
                            statement.setString(1, uuid.toString());
                            try (ResultSet results = statement.executeQuery()) {
                                if (results.next()) {
                                    Object object = results.getObject(label);
                                    callback.accept(object.toString());
                                    if (!(playerDataType == PlayerDataType.UUID)) // don't update the uuid
                                       dataCache.update(playerDataType, object.toString());
                                    return;
                                }
                                callback.accept(null);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
    }

    /**
     * Updates the given DataType column with the given object
     * It uses transactions, to ensure that other updates don't interfere
     * with each other.
     * Data is also updated in the redis cache.
     *
     * @param playerDataType DataType which will be updated with the Object
     * @param object         Object to update the DataType with
     */
    public void update(PlayerDataType playerDataType, Object object) {
        dataCache.update(playerDataType, object.toString());

        if (playerDataType == PlayerDataType.LEVEL) {
            PlayerLevel playerLevel = new PlayerLevel(core, uuid);
            playerLevel.setLevel(Integer.parseInt(object.toString()));
            return;
        }

        String label = playerDataType.getColumnName();

        Connection connection = core.getMariaDB().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + tableName + "(uuid, " + label + ") VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE " + label + " = VALUES(" + label + ")")) {
            statement.setString(1, uuid.toString());
            statement.setObject(2, object);
            connection.setAutoCommit(false); // disable auto-commit mode to start a transaction
            statement.executeUpdate();
            connection.commit();
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