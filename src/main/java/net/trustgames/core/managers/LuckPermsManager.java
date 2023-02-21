package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.trustgames.core.Core;
import net.trustgames.core.player.tablist.TablistTeams;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the various LuckPerms checks and events
 */
public class LuckPermsManager {

    private final Core core;

    public LuckPermsManager(Core core) {
        this.core = core;
    }

    static final LuckPerms luckPerms = Core.getLuckPerms();

    /**
     * @param player What player to check on
     * @param group What group check for
     * @return if the given player is in the given group
     */
    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    /**
     * @return Set of all loaded groups
     */
    public static Set<Group> getGroups() {
        return luckPerms.getGroupManager().getLoadedGroups();
    }

    /**
     * Returns the first group it matches from the list
     *
     * @param player Player to check on
     * @param possibleGroups List of groups to check for
     * @return Player's group found from the list
     */
    public static String getPlayerGroupFromList(Player player, Collection<String> possibleGroups) {
        for (String group : possibleGroups) {
            if (player.hasPermission("group." + group)) {
                return group;
            }
        }
        return null;
    }

    /**
     * @param uuid UUID of Player to check primary group for
     * @return Primary group of the given player
     */
    public static String getPlayerPrimaryGroup(UUID uuid) {
        return Objects.requireNonNull(luckPerms.getUserManager().getUser(uuid),
                "Player UUID was null when getting his primary group")
                .getPrimaryGroup();
    }

    /**
     * Get the GroupManager of LuckPerms
     *
     * @return LuckPerms GroupManager
     */
    public static GroupManager getGroupManager() {
        return luckPerms.getGroupManager();
    }

    /**
     * Get the player's prefix. If the prefix is null,
     * it will be set to ""
     *
     * @param uuid UUID of Player to get prefix for
     * @return Player prefix String
     */
    public static @NotNull Component getPlayerPrefix(UUID uuid){
        User user = luckPerms.getUserManager().getUser(uuid);
        Component prefix = Component.text("");
        if (user != null){
            String prefixString = user.getCachedData().getMetaData().getPrefix();
            prefix = ColorUtils.color(Objects.requireNonNullElse(prefixString, ""));
        }
        return prefix;
    }

    /**
     * Get the group's prefix. If the prefix is null,
     * it will be set to ""
     *
     * @param group Group to get prefix for
     * @return Group prefix String
     */
    public static @NotNull Component getGroupPrefix(Group group){
        String prefixString = group.getCachedData().getMetaData().getPrefix();
        return ColorUtils.color(Objects.requireNonNullElse(prefixString, ""));
    }

    /**
     * @param player Player to convert to User
     * @return User from the given Player
     */
    public static User getUser(Player player) {
        return luckPerms.getPlayerAdapter(Player.class).getUser(player);
    }

    public void registerListeners() {
        EventBus eventBus = Core.getLuckPerms().getEventBus();

        eventBus.subscribe(core, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    /**
     * @param event Every data recalculation of the user
     */
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        core.getServer().getScheduler().runTask(core, () -> {
            UUID uuid = event.getUser().getUniqueId();

            // add player to player-list team to sort priority
            TablistTeams playerListTeamsManager = new TablistTeams(core);
            playerListTeamsManager.addToTeam(uuid);
        });
    }
}
