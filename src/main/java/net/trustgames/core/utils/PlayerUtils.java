package net.trustgames.core.utils;

import org.bukkit.Bukkit;
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
     */
    public static Player getOnlinePlayer(String uuidOrName) {
        Player player;
        try {
            UUID uuid = UUID.fromString(uuidOrName);
            player = Bukkit.getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            player = Bukkit.getPlayer(uuidOrName);
        }
        return player;
    }
}
