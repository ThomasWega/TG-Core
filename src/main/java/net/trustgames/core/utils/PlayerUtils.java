package net.trustgames.core.utils;

import net.trustgames.core.cache.OfflinePlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerUtils {

    /**
     * check if the supplied string is uuid or a player name by
     * trying to convert it to uuid. If it succeeds, its uuid and
     * the offlinePlayer's is retrieved by uuid. If it fails
     * and throws exception, it's the player's name and the is retrieved
     * by string name.
     * <p>
     * DOESN'T WORK WITH OFFLINE PLAYERS!
     *
     * @see PlayerUtils#getOfflinePlayer(String)
     */
    public static Player getPlayer(String uuidOrName) {
        Player player;
        try {
            UUID uuid = UUID.fromString(uuidOrName);
            player = Bukkit.getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            player = Bukkit.getPlayer(uuidOrName);
        }
        return player;
    }

    /**
     * check if the supplied string is uuid or a player name by
     * trying to convert it to uuid. If it succeeds, its uuid and
     * the offlinePlayer's is retrieved by uuid. If it fails
     * and throws exception, it's the player's name and the is retrieved
     * by string name.
     * <p>
     * Also works with offline player. For online players, refer to "See Also"
     *
     * @see PlayerUtils#getPlayer(String)
     */
    public static OfflinePlayer getOfflinePlayer(String uuidOrName) {
        OfflinePlayer offlinePlayer;
        try {
            UUID uuid = UUID.fromString(uuidOrName);
            offlinePlayer = OfflinePlayerCache.getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            offlinePlayer = OfflinePlayerCache.getPlayer(uuidOrName);
        }
        return offlinePlayer;
    }
}
