package net.trustgames.core.tablist;

import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.trustgames.core.logger.CoreLogger;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles the priority sorting of groups and players in the player-list
 */
public final class TablistTeams {

    static final HashMap<Group, Integer> groupOrder = new HashMap<>();
    private final Scoreboard tablist;

    public TablistTeams(Scoreboard tablist) {
        this.tablist = tablist;
    }

    /**
     * Create all the teams by getting all groups from LuckPerms and putting each group in map
     * with its corresponding weight. Then register new team with the first parameter weight, and second
     * parameter the name of the group. Example: "20vip"
     */
    public void createTeams() {
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
     * @param player Player to add to the scoreboard team
     */
    public void addToTeam(Player player) {
        Group playerGroup = LuckPermsManager.getGroupManager().getGroup(LuckPermsManager.getPlayerPrimaryGroup(player));
        if (playerGroup == null) return;
        String stringTeam = groupOrder.get(playerGroup) + playerGroup.getName();
        Team team = tablist.getTeam(stringTeam);

        if (team == null) {
            CoreLogger.LOGGER.severe("Scoreboard team " + stringTeam + " wasn't found");
            return;
        }

        team.addPlayer(player);

        if (!stringTeam.contains("default"))
            team.prefix(LuckPermsManager.getPlayerPrefix(player).append(Component.text(" ")));

        player.setScoreboard(tablist);
    }

    /**
     * @param player Player to remove from the scoreboard team
     */
    public void removeFromTeam(Player player) {
        if (player == null) return;
        Team team = tablist.getPlayerTeam(player);
        if (team != null)
            team.removePlayer(player);
    }
}
