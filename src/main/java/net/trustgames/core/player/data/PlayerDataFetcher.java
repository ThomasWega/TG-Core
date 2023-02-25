package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.data.PlayerDataDB.tableName;

/**
 * This class is used to fetch and update the player data database table
 */
public class PlayerDataFetcher {

    private final Core core;
    private final UUID uuid;

    public PlayerDataFetcher(Core core, UUID uuid) {
        this.core = core;
        this.uuid = uuid;
    }

    /**
     * Get the player data Object from the column "label" that corresponds
     * to the given uuid. This whole operation is run async, and the result is saved
     * in the callback
     *
     * @param playerDataType DataType which will be used to get the column name
     * @param callback       Callback where the result will be saved
     */
    public void fetch(PlayerDataTypes playerDataType, Consumer<Object> callback) {
        String label = playerDataType.getColumnName();
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("SELECT " + label + " FROM " + tableName + " WHERE uuid = ?")) {
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
     * Updates the given DataType column with the given object
     *
     * @param playerDataType DataType which will be updated with the Object
     * @param object         Object to update the DataType with
     */
    public void update(PlayerDataTypes playerDataType, Object object) {
        String label = playerDataType.getColumnName();
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement(
                    "INSERT INTO " + tableName + "(uuid, " + label + ") VALUES (?, ?) ON DUPLICATE KEY UPDATE " + label + " = VALUES(" + label + ")")) {
                statement.setString(1, uuid.toString());
                statement.setObject(2, object);

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
