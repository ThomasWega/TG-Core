package net.trustgames.core.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.trustgames.core.Core;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Objects;

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

    public static String getPlayerPrimaryGroup(Player player){
        return Objects.requireNonNull(luckPerms.getUserManager().getUser(player.getUniqueId())).getPrimaryGroup();
    }

    public static boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static GroupManager getGroupManager(){
        return luckPerms.getGroupManager();
    }

    public static User getUser(Player player){
        return luckPerms.getUserManager().getUser(player.getUniqueId());
    }
}
