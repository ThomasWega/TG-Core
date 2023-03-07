package net.trustgames.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class OfflinePlayerCache {

    /**
     * Cache that holds the uuids by players objects and expires every 6 hours
     */
    private static final LoadingCache<OfflinePlayer, UUID> uuidCache = Caffeine.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(OfflinePlayer::getUniqueId);

    /**
     * Cache that holds the players objects by uuids and expires every 6 hours
     */
    private static final LoadingCache<UUID, OfflinePlayer> playerCache = Caffeine.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(Bukkit::getOfflinePlayer);

    /**
     * Cache that holds the players objects by names and expires every 6 hours
     */
    private static final LoadingCache<String, OfflinePlayer> nameCache = Caffeine.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(Bukkit::getOfflinePlayer);

    /**
     * Get the UUID of the player from the cache
     *
     * @param player Player to get uuid from
     * @return UUID of given Player
     */
    public static UUID getUUID(OfflinePlayer player) {
        try {
            return uuidCache.get(player);
        } catch (RuntimeException e) {
            return player.getUniqueId();
        }
    }

    /**
     * Get the Player of the uuid from the cache
     *
     * @param uuid UUID to get the Player from
     * @return Player of given UUID
     */
    public static OfflinePlayer getPlayer(UUID uuid) {
        try {
            return playerCache.get(uuid);
        } catch (RuntimeException e) {
            return Bukkit.getOfflinePlayer(uuid);
        }
    }

    /**
     * Get the Player of the uuid from the cache
     *
     * @param name Name to get the Player from
     * @return Player of given UUID
     */
    public static OfflinePlayer getPlayer(String name) {
        try {
            return nameCache.get(name);
        } catch (RuntimeException e) {
            return Bukkit.getOfflinePlayer(name);
        }
    }
}
