package net.trustgames.core.player.uuid;

import net.trustgames.core.Core;
import net.trustgames.core.cache.NameCache;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.uuid.PlayerUUIDDB.tableName;

public class PlayerUUIDFetcher implements Listener {

    private final Core core;
    private final NameCache nameCache;

    public PlayerUUIDFetcher(Core core) {
        this.core = core;
        this.nameCache = new NameCache(core);
    }

    public void write(UUID uuid, String playerName){
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO " + tableName + "(uuid, name) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name)"))
            {
                statement.setString(1, uuid.toString());
                statement.setString(2, playerName);

                statement.executeUpdate();

                nameCache.update(uuid, playerName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fetch(UUID uuid, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
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
}
