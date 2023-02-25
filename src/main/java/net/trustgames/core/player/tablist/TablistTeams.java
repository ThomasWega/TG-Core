package net.trustgames.core.player.tablist;

import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.trustgames.core.Core;
import net.trustgames.core.logger.CoreLogger;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Handles the priority sorting of groups and players in the player-list
 */
public class TablistTeams {

    static final HashMap<Group, Integer> groupOrder = new HashMap<>();
    private final Core core;
    private Scoreboard tablist;

    public TablistTeams(Core core) {
        this.core = core;
    }

    /**
     * Create all the teams by getting all groups from LuckPerms and putting each group in map
     * with its corresponding weight. Then register new team with the first parameter weight, and second
     * parameter the name of the group. Example: "20vip"
     */
    public void createTeams() {

        tablist = core.getTablistScoreboard();

        int i = 0;

        HashMap<Group, Integer> groupWeight = new HashMap<>();

        // get the groups and put the name and weight to the map
        for (Group group : LuckPermsManager.getGroups()) {
            if (group.getWeight().isPresent()) {
                groupWeight.put(group, group.getWeight().getAsInt());
            } else {
                CoreLogger.LOGGER.severe("LuckPerms group " + group.getName() + " doesn't have any weight! Setting the weight to 1...");

                Objects.requireNonNull(LuckPermsManager.getGroupManager().getGroup(group.getName()),
                                "Group " + group.getName() + " wasn't found when setting a missing weight")
                        .data().add(Node.builder("weight.1").build());

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

            Team team = tablist.registerNewTeam(i + "" + group.getName());
            Component prefix = LuckPermsManager.getGroupPrefix(group);
            if (!group.getName().equals("default"))
                team.prefix(prefix.append(Component.text(" ")));
            i++;
        }
    }

    /**
     * Add player to the corresponding team by getting his primary group and its group's weight.
     * set the prefix to team with luckperms cached data.
     *
     * @param uuid UUID of whom to add to the scoreboard team
     */
    public void addToTeam(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        tablist = core.getTablistScoreboard();
        Group playerGroup = LuckPermsManager.getGroupManager().getGroup(LuckPermsManager.getPlayerPrimaryGroup(uuid));
        if (playerGroup == null) return;
        String stringTeam = groupOrder.get(playerGroup) + playerGroup.getName();
        Team team = tablist.getTeam(stringTeam);

        if (team == null) {
            CoreLogger.LOGGER.severe("Scoreboard team " + stringTeam + " wasn't found");
            return;
        }

        team.addPlayer(player);

        if (!stringTeam.contains("default"))
            team.prefix(LuckPermsManager.getPlayerPrefix(uuid).append(Component.text(" ")));

        player.setScoreboard(tablist);
    }

    /**
     * @param uuid UUID of whom to remove from the scoreboard team
     */
    public void removeFromTeam(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        tablist = core.getTablistScoreboard();
        Team team = tablist.getPlayerTeam(player);
        if (team != null)
            team.removePlayer(player);
    }
}
