package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.uuid_name.PlayerIDFetcher;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public final class UUIDCache {

    private static final String field = PlayerDataType.UUID.getColumnName();

    private final JedisPool pool;

    private final Core core;

    public UUIDCache(Core core) {
        this.core = core;
        this.pool = core.getJedisPool();
    }

    /**
     * Get the UUID of the player from the cache.
     *
     * @param playerName Name of the player to get UUID for.
     * @param callback Where the UUID of the player, or null will be saved
     */
    public void get(@NotNull String playerName, Consumer<UUID> callback){
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String uuidString = jedis.hget(playerName, field);
                if (uuidString == null) {
                    PlayerIDFetcher idFetcher = new PlayerIDFetcher(core);
                    idFetcher.fetchUUID(playerName, uuid -> {
                        put(playerName, uuid);
                        callback.accept(uuid);
                    });
                    return;
                }
                callback.accept(UUID.fromString(uuidString));
            }
        });
    }

    /**
     * Put a player's UUID in the cache.
     *
     * @param playerName Name of the player.
     * @param uuid       UUID of the player.
     */
    public void put(@NotNull String playerName, @NotNull UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(playerName, field, uuid.toString());
        }
    }
}
