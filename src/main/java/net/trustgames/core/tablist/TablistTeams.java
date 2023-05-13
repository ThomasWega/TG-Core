package net.trustgames.core.tablist;

import jline.internal.Nullable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.trustgames.core.Core;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles the priority sorting of groups and players in the player-list
 */
public final class TablistTeams {

    private static final Logger logger = Core.LOGGER;

    private static final HashMap<Group, Integer> groupOrder = new HashMap<>();
    @Getter
    private static final Scoreboard tablist = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Plugin plugin;

    public TablistTeams(Plugin plugin) {
        this.plugin = plugin;
        create();
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        eventBus.subscribe(plugin, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
        eventBus.subscribe(plugin, GroupCreateEvent.class, this::onGroupCreation);
    }

    /**
     * Create all the teams by getting all groups from LuckPerms and putting each group in map
     * with its corresponding weight. Then register new team with the first parameter weight, and second
     * parameter the name of the group. Example: "20vip"
     */
    private static void create() {
        int i = 0;

        HashMap<Group, Integer> groupWeight = new HashMap<>();

        // get the groups and put the name and weight to the map
        for (Group group : LuckPermsManager.getGroups()) {
            if (group.getWeight().isPresent()) {
                groupWeight.put(group, group.getWeight().getAsInt());
            } else {
                logger.warning("LuckPerms group " + group.getName() + " doesn't have any weight! Setting the weight to 1...");
                group.data().add(Node.builder("weight.1").build());

                LuckPermsManager.getGroupManager().saveGroup(group);
                groupWeight.put(group, group.getWeight().getAsInt());
            }
        }

        /*
         order the map by highest value and put every group to a new ordered map with "i" value.
         also register a new team with (i + name). The lower "i", the highest order priority.
         Example: 1prime is lower then 0admin
        */
        for (Group group : groupWeight.entrySet()
                .stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey).toList()) {
            groupOrder.put(group, i);

            if (group == null) return;

            String teamName = i + group.getName();
            if (tablist.getTeam(teamName) == null) {
                Team team = tablist.registerNewTeam(i + "" + group.getName());
                Component prefix = LuckPermsManager.getGroupPrefix(group);
                if (!group.getName().equals("default"))
                    team.prefix(prefix.append(Component.text(" ")));
            }
            i++;
        }
    }

    /**
     * Add player to the corresponding team by getting his primary group and its group's weight.
     * set the prefix to team with luckperms cached data.
     *
     * @param player Player to add to the scoreboard team
     */
    public static void addPlayer(@NotNull Player player) {
        String group = LuckPermsManager.getUser(player).getPrimaryGroup();
        Group playerGroup = LuckPermsManager.getGroupManager().getGroup(group);
        if (playerGroup == null) return;
        String stringTeam = groupOrder.get(playerGroup) + playerGroup.getName();
        Team team = tablist.getTeam(stringTeam);

        if (team == null) {
            logger.severe("Scoreboard team " + stringTeam + " wasn't found");
            return;
        }

        team.addPlayer(player);

        if (!stringTeam.contains("default"))
            team.prefix(LuckPermsManager.getPlayerPrefix(player).append(Component.text(" ")));

        player.setScoreboard(tablist);
    }

    public static void removePlayer(@Nullable Player player) {
        if (player == null) return;
        Team team = tablist.getPlayerTeam(player);
        if (team != null)
            team.removePlayer(player);
    }

    /**
     * On change of data of the player in luckperms. This is to make sure that if
     * his group changes, the scoreboard will update his team as well.
     */
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            if (player == null) return;

            TablistTeams.addPlayer(player);
        });
    }

    /**
     * If a new group is created, make sure to recreate all the teams
     * and reassign players
     */
    private void onGroupCreation(GroupCreateEvent event) {
        create();

        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player);
        }
    }
}
