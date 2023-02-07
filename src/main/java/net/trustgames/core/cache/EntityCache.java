package net.trustgames.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.bukkit.entity.Entity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles the player's uuids and caching with expiration
 */
public class EntityCache {

    /**
     * Cache that holds the players uuid and expires every 6 hours
     */
    private static final LoadingCache<Entity, UUID> cache = Caffeine.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(Entity::getUniqueId);

    /**
     * Get the UUID of the player from the cache
     *
     * @param player Player to get uuid for
     * @return UUID of given Player
     */
    public static UUID getUUID(Entity player) {
        try {
            return cache.get(player);
        } catch (RuntimeException e) {
            return player.getUniqueId();
        }
    }
}
