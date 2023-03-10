package net.trustgames.core.player.uuid;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import static net.trustgames.core.player.uuid.PlayerUUIDDB.tableName;

public class PlayerUUIDHandler implements Listener {

    private static final JedisPool pool = Core.pool;

    private final Core core;

    public PlayerUUIDHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID uuid = UUIDCache.get(playerName);
        write(uuid, playerName);
    }

    private void write(UUID uuid, String playerName){
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO " + tableName + "(uuid, name) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name)"))
            {
                statement.setString(1, uuid.toString());
                statement.setString(2, playerName);

                statement.executeUpdate();
                updateCache(uuid, playerName);
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

    private void updateCache(UUID uuid, String playerName) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.set(uuid.toString(), playerName);
            }
        });
    }

    public void getName(UUID uuid, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String name = jedis.get(uuid.toString());
                if (name == null){
                    fetch(uuid, fetchName -> {
                        System.out.println(fetchName);
                        callback.accept(fetchName);
                        updateCache(uuid, fetchName);
                    });
                }
                callback.accept(name);
            }
        });
    }
}
