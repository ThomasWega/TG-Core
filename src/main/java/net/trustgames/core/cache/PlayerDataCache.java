package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerDataCache {

    private final Core core;
    private final OfflinePlayer player;

    private static final JedisPool pool = Core.pool;

    public PlayerDataCache(Core core, UUID uuid) {
        this.core = core;
        this.player = Bukkit.getOfflinePlayer(uuid);
    }

    public void update(PlayerDataType dataType, String value) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.hset(player.getName(), dataType.getColumnName(), value);
                jedis.expire(player.getName(), 60); // expire every 60 seconds
            }
        });
    }

    public void fetch(PlayerDataType dataType, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            String playerName = player.getName();
            try (Jedis jedis = pool.getResource()) {
                String result = jedis.hget(playerName, dataType.getColumnName());
                callback.accept(result);
            }
        });
    }
}
