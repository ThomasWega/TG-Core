package net.trustgames.core.cache;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public final class UUIDCache {

    private static final JedisPool pool = Core.pool;

    /**
     * Get the UUID of the player from the cache.
     *
     * @param playerName Name of the player to get UUID for.
     * @return UUID of the player, or null if not found in cache.
     */
    public static UUID get(String playerName){
        try (Jedis jedis = pool.getResource()) {
            String uuidString = jedis.hget(playerName, "uuid");
            if (uuidString == null) {
                UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
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
    public static void put(String playerName, UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(playerName,"uuid", uuid.toString());
            jedis.expire(playerName, 1800); // expire after 30 minutes (in seconds)
        }
    }
}
