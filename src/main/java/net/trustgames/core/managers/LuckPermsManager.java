package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.trustgames.core.Core;
import net.trustgames.core.tablist.TablistTeams;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the various LuckPerms checks and events
 */
public final class LuckPermsManager {

    private final Core core;

    public LuckPermsManager(Core core) {
        this.core = core;
        // register events
        LuckPermsProvider.get().getEventBus().subscribe(core,
                UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    /**
     * @param player What player to check on
     * @param group  What group check for
     * @return if the given player is in the given group
     */
    public static boolean isPlayerInGroup(@NotNull Player player, @NotNull String group) {
        return player.hasPermission("group." + group);
    }

    /**
     * @return Set of all loaded groups
     */
    public static @NotNull Set<Group> getGroups() {
        LuckPerms luckPerms = LuckPermsProvider.get();
        return luckPerms.getGroupManager().getLoadedGroups();
    }

    /**
     * Returns the first group it matches from the list
     *
     * @param player         Player to check on
     * @param possibleGroups List of groups to check for
     * @return Player's group found from the list
     */
    public static @Nullable String getPlayerGroupFromList(@NotNull Player player,
                                                          @NotNull Collection<String> possibleGroups) {
        for (String group : possibleGroups) {
            if (player.hasPermission("group." + group)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Get the GroupManager of LuckPerms
     *
     * @return LuckPerms GroupManager
     */
    public static GroupManager getGroupManager() {
        LuckPerms luckPerms = LuckPermsProvider.get();
        return luckPerms.getGroupManager();
    }

    /**
     * Get the player's prefix. If the prefix is null,
     * it will be set to ""
     *
     * @param player Player to get prefix for
     * @return Player prefix String
     */
    public static @NotNull Component getPlayerPrefix(@NotNull Player player) {
        User user = getUser(player);
        String prefixString = user.getCachedData().getMetaData().getPrefix();

        return ColorUtils.color(Objects.requireNonNullElse(prefixString, ""));
    }

    /**
     * Get the group's prefix. If the prefix is null,
     * it will be set to ""
     *
     * @param group Group to get prefix for
     * @return Group prefix String
     */
    public static @NotNull Component getGroupPrefix(@NotNull Group group) {
        String prefixString = group.getCachedData().getMetaData().getPrefix();
        return ColorUtils.color(Objects.requireNonNullElse(prefixString, ""));
    }

    /**
     * @param player Player to convert to User
     * @return User from the given Player
     */
    public static @NotNull User getUser(@NotNull Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        return luckPerms.getPlayerAdapter(Player.class).getUser(player);
    }

    /**
     * @param event Every data recalculation of the user
     */
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        core.getServer().getScheduler().runTask(core, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            if (player == null) return;

            // add player to player-list team to sort priority
            TablistTeams.addToTeam(player);
        });
    }
}
