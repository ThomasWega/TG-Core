package net.trustgames.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class OfflinePlayerCache {

    /**
     * Cache that holds the players uuid and expires every 6 hours
     */
    private static final LoadingCache<OfflinePlayer, UUID> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull UUID load(@NotNull OfflinePlayer player) {
                    return player.getUniqueId();
                }
            });

    /**
     * Get the UUID of the player from the cache
     *
     * @param player Player to get uuid for
     * @return UUID of given Player
     */
    public static UUID getUUID(OfflinePlayer player) {
        try {
            return cache.get(player);
        } catch (ExecutionException e) {
            return player.getUniqueId();
        }
    }
}
