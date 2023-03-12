package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.player.uuid_name.PlayerIDFetcher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public class NameCache {

    private final Core core;

    private final JedisPool pool;

    public NameCache(Core core) {
        this.core = core;
        this.pool = core.getJedisPool();
    }

    public void update(UUID uuid, String playerName) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.set(uuid.toString(), playerName);
            }
        });
    }

    public void get(UUID uuid, Consumer<String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String name = jedis.get(uuid.toString());
                if (name == null){
                    PlayerIDFetcher uuidFetcher = new PlayerIDFetcher(core);
                    uuidFetcher.fetchName(uuid, fetchName -> {
                        callback.accept(fetchName);
                        update(uuid, fetchName);
                    });
                }
                callback.accept(name);
            }
        });
    }
}
