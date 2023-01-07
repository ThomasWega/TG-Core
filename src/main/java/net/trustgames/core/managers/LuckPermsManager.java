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

public class LuckPermsManager {

    private final Core core;

    public LuckPermsManager(Core core) {
        this.core = core;
    }

    static LuckPerms luckPerms = Core.getLuckPerms();

    // check if player is in defined group
    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    // get set of groups
    public static Set<Group> getGroups() {
        return luckPerms.getGroupManager().getLoadedGroups();
    }

    // check if player is in group from defined list
    public static String getPlayerGroupFromList(Player player, Collection<String> possibleGroups) {
        for (String group : possibleGroups) {
            if (player.hasPermission("group." + group)) {
                return group;
            }
        }
        return null;
    }

    // gets the player's primary group
    public static String getPlayerPrimaryGroup(Player player) {
        return Objects.requireNonNull(luckPerms.getUserManager().getUser(player.getUniqueId())).getPrimaryGroup();
    }

    // return the group manager of luckperms
    public static GroupManager getGroupManager() {
        return luckPerms.getGroupManager();
    }

    // get the user instance from player instance
    public static User getUser(Player player) {
        return luckPerms.getPlayerAdapter(Player.class).getUser(player);
    }

    // register listeners for luckperms
    public void registerListeners() {
        // register the user data change event
        EventBus eventBus = Core.getLuckPerms().getEventBus();
        eventBus.subscribe(core, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    // on luckperms data change event
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        core.getServer().getScheduler().runTask(core, () -> {
            User user = event.getUser();

            // add player to playerlist team to sort correctly
            PlayerListTeams playerListTeams = new PlayerListTeams(core);
            playerListTeams.addToTeam(Bukkit.getPlayer(user.getUniqueId()));
        });
    }
}
