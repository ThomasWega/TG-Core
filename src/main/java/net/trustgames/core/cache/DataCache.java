package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public class DataCache {

    private final Core core;
    private final OfflinePlayer player;
    private final UUID uuid;

    private final JedisPool pool;


    public DataCache(Core core, UUID uuid) {
        this.core = core;
        this.uuid = uuid;
        this.pool = core.getJedisPool();
        this.player = Bukkit.getServer().getOfflinePlayer(uuid);
    }

    public void update(PlayerDataType dataType, String value) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String column = dataType.getColumnName();
                jedis.hset(player.getName(), column, value);
            }
        });
    }

    public void fetch(PlayerDataType dataType, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            String playerName = player.getName();
            try (Jedis jedis = pool.getResource()) {
                String result = jedis.hget(playerName, dataType.getColumnName());
                if (result == null){
                    PlayerDataFetcher dataFetcher = new PlayerDataFetcher(core, uuid);
                    dataFetcher.fetch(dataType, data -> update(dataType, data));
                }
                callback.accept(result);
            }
        });
    }
}
