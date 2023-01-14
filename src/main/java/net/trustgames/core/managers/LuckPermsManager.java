package net.trustgames.core.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.trustgames.core.Core;
import net.trustgames.core.playerlist.PlayerListTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

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
     * check if player is in defined group
     *
     * @param player What player to check on
     * @param group What group to check for
     * @return if the given player is in the given group
     */
    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    /**
     * Get set of all loaded groups
     *
     * @return Set of all loaded groups
     */
    public static Set<Group> getGroups() {
        return luckPerms.getGroupManager().getLoadedGroups();
    }

    /**
     * Check if player is in group from defined list
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
     * Gets the player's primary group
     *
     * @param player Player to check primary group for
     * @return Primary group of the given player
     */
    public static String getPlayerPrimaryGroup(Player player) {
        return Objects.requireNonNull(luckPerms.getUserManager().getUser(player.getUniqueId())).getPrimaryGroup();
    }

    /**
     * Return the group manager of luckperms
     *
     * @return LuckPerms GroupManager
     */
    public static GroupManager getGroupManager() {
        return luckPerms.getGroupManager();
    }

    /**
     * Get the user instance from player instance
     *
     * @param player Player to convert to User
     * @return User from given Player
     */
    public static User getUser(Player player) {
        return luckPerms.getPlayerAdapter(Player.class).getUser(player);
    }

    /**
     * Register listeners for luckperms
     */
    public void registerListeners() {
        // register the user data change event
        EventBus eventBus = Core.getLuckPerms().getEventBus();
        eventBus.subscribe(core, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    /**
     * On luckperms user's data change
     *
     * @param event Every data recalculation of the user
     */
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        core.getServer().getScheduler().runTask(core, () -> {
            User user = event.getUser();

            // add player to playerlist team to sort correctly
            PlayerListTeams playerListTeams = new PlayerListTeams(core);
            playerListTeams.addToTeam(Bukkit.getPlayer(user.getUniqueId()));
        });
    }
}
