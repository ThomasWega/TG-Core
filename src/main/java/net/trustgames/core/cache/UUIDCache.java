package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.config.cache.player_data.PlayerDataType;
import net.trustgames.core.config.cache.player_uuid.PlayerUUIDUpdate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public final class UUIDCache {

    private static final String field = PlayerDataType.UUID.getColumnName();

    private static final JedisPool pool = Core.jedisPool;

    /**
     * Get the UUID of the player from the cache.
     *
     * @param playerName Name of the player to get UUID for.
     * @return UUID of the player, or null if not found in cache.
     */
    public static UUID get(@NotNull String playerName){
        try (Jedis jedis = pool.getResource()) {
            String uuidString = jedis.hget(playerName, field);
            if (uuidString == null) {
                UUID uuid = Bukkit.getServer().getOfflinePlayer(playerName).getUniqueId();
                UUIDCache.put(playerName, uuid);
                return uuid;
            }
            return UUID.fromString(uuidString);
        }
    }

    /**
     * Put a player's UUID in the cache.
     *
     * @param playerName Name of the player.
     * @param uuid       UUID of the player.
     */
    public static void put(@NotNull String playerName, @NotNull UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(playerName, field, uuid.toString());
            jedis.expire(playerName, PlayerUUIDUpdate.INTERVAL.value);
        }
    }
}
