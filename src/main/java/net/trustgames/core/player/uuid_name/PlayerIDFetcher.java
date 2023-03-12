package net.trustgames.core.player.uuid_name;

import net.trustgames.core.Core;
import net.trustgames.core.cache.NameCache;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.uuid_name.PlayerIDDB.tableName;

public class PlayerIDFetcher implements Listener {

    private final Core core;

    public PlayerIDFetcher(Core core) {
        this.core = core;
    }

    public void write(UUID uuid, String playerName){
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO " + tableName + "(uuid, name) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name)"))
            {
                statement.setString(1, uuid.toString());
                statement.setString(2, playerName);

                statement.executeUpdate();

                NameCache nameCache = new NameCache(core);
                nameCache.update(uuid, playerName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fetchName(UUID uuid, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT name FROM " + tableName + " WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();
                if (result.next()){
                    callback.accept(result.getString("name"));
                    return;
                }
                callback.accept(null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fetchUUID(String playerName, Consumer<UUID> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM " + tableName + " WHERE name = ?")) {
                statement.setString(1, playerName);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    String stringUuid = result.getString("uuid");
                    try {
                        callback.accept(UUID.fromString(stringUuid));
                        return;
                    } catch (IllegalArgumentException e){
                        throw new RuntimeException("INVALID UUID FOR: " + stringUuid, e);
                    }
                }
                callback.accept(null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
