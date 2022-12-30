package net.trustgames.core.managers;

import net.luckperms.api.LuckPerms;
import net.trustgames.core.Core;
import org.bukkit.entity.Player;

import java.util.Collection;

public class LuckPermsManager {

    static LuckPerms luckPerms = Core.getLuckPerms();

    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    public static String getPlayerGroupFromList(Player player, Collection<String> possibleGroups) {
        for (String group : possibleGroups) {
            if (player.hasPermission("group." + group)) {
                return group;
            }
        }
        return null;
    }

    public static String getPlayerGroup(Player player){
        return luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
    }
}
