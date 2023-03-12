package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataFetcher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerDataCache {

    private final Core core;
    private final JedisPool pool;
    private final UUID uuid;
    private final PlayerDataType dataType;



    public PlayerDataCache(Core core, UUID uuid, PlayerDataType dataType) {
        this.core = core;
        this.uuid = uuid;
        this.pool = core.getJedisPool();
        this.dataType = dataType;
    }

    public void update(String value) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String column = dataType.getColumnName();
                jedis.hset(uuid.toString(), column, value);
            }
        });
    }

    public void get(Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String result = jedis.hget(uuid.toString(), dataType.getColumnName());
                if (result == null){
                    PlayerDataFetcher dataFetcher = new PlayerDataFetcher(core, dataType);
                    dataFetcher.fetch(uuid, data -> {
                        // if still null, there is no data on the player even in the database
                        if (data != null)
                            update(data);
                        callback.accept(data);
                    });
                    return;
                }
                callback.accept(result);
            }
        });
    }
}
