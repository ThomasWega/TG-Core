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
     * @param player What player to check on
     * @param group What group to check for
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
     * @param player Player to check primary group for
     * @return Primary group of the given player
     */
    public static String getPlayerPrimaryGroup(Player player) {
        return Objects.requireNonNull(luckPerms.getUserManager().getUser(player.getUniqueId()),
                "Player UUID was null when getting his primary group")
                .getPrimaryGroup();
    }

    public static GroupManager getGroupManager() {
        return luckPerms.getGroupManager();
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
            User user = event.getUser();

            // add player to player-list team to sort priority
            PlayerListTeams playerListTeams = new PlayerListTeams(core);
            playerListTeams.addToTeam(Bukkit.getPlayer(user.getUniqueId()));
        });
    }
}
