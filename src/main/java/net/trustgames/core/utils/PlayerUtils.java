package net.trustgames.core.utils;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerUtils {

    private static final ConcurrentHashMap<Player, UUID> cache = new ConcurrentHashMap<>();

    public static UUID getUUID(Player player) {
        UUID uuid = cache.get(player);
        if (uuid == null) {
            uuid = player.getUniqueId();
            cache.put(player, uuid);
        }
        return uuid;
    }
}
