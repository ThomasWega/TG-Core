package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataType;

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

    public PlayerDataFetcher(Core core, UUID uuid) {
        this.core = core;
        this.uuid = uuid;
    }

    /**
     * Get the player data Object from the column "label" that corresponds
     * to the given uuid. This whole operation is run async, and the result is saved
     * in the callback. If no result is found, int "0" is returned
     *
     * @param playerDataType DataType which will be used to get the column name
     * @param callback       Callback where the result will be saved
     */
    public void fetch(PlayerDataType playerDataType, Consumer<Object> callback) {
        String label = playerDataType.getColumnName();
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT " + label + " FROM " + tableName + " WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        Object object = results.getObject(label);
                        callback.accept(object);
                        return;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            callback.accept(null);
        });
    }

    /**
     * Updates the given DataType column with the given object.
     * It uses transactions, to ensure that other updates don't interfere
     * with each other.
     *
     * @param playerDataType DataType which will be updated with the Object
     * @param object         Object to update the DataType with
     */
    public void update(PlayerDataType playerDataType, Object object) {
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