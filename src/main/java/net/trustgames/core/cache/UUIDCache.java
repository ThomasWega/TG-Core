package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.player_data.PlayerDataConfig;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataFetcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

public final class UUIDCache {

    private static final String field = PlayerDataType.UUID.getColumnName();

    private final JedisPool pool;

    private final Core core;
    private final String playerName;

    public UUIDCache(@NotNull Core core, @NotNull String playerName) {
        this.core = core;
        this.pool = core.getJedisPool();
        this.playerName = playerName;
    }

    /**
     * Get the UUID of the player from the cache.
     * If it's not in the cache, it gets it from the database table
     * and puts it in the cache.
     * If still null, it gets it from mojang api.
     *
     * @param callback Where the UUID of the player, or null will be saved
     */
    public void get(Consumer<@Nullable UUID> callback){
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String uuidString = jedis.hget(playerName, field);
                jedis.expire(playerName, PlayerDataConfig.DATA_EXPIRY.getSeconds());
                if (uuidString == null) {
                    PlayerDataFetcher dataFetcher = new PlayerDataFetcher(core, PlayerDataType.UUID);
                    dataFetcher.fetchUUID(playerName, uuid -> {
                        // if still null, there is no data on the player even in the database
                        if (uuid != null)
                            update(uuid);
                        callback.accept(uuid);
                    });
                    return;
                }
                callback.accept(UUID.fromString(uuidString));
            }
        });
    }

    /**
     * Updates the player's UUID in the cache.
     *
     * @param uuid       UUID of the player.
     */
    public void update(@NotNull UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(playerName, field, uuid.toString());
        }
    }
}
