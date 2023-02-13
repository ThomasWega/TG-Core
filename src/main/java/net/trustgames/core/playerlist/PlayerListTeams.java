package net.trustgames.core.playerlist;

import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.trustgames.core.Core;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Handles the priority sorting of groups and players in the player-list
 */
public class PlayerListTeams {

    private final Core core;
    private Scoreboard playerListScoreboard;
    public PlayerListTeams(Core core) {
        this.core = core;
    }

    static final TreeMap<String, Integer> groupOrder = new TreeMap<>();


    /** Create all the teams by getting all groups from LuckPerms and putting each group in map
    with its corresponding weight. Then register new team with the first parameter weight, and second
    parameter the name of the group. Example: "20vip"
     */
    public void createTeams() {

        playerListScoreboard = core.getPlayerListScoreboard();

        int i = 0;

        TreeMap<String, Integer> groupWeight = new TreeMap<>();

        // get the groups and put the name and weight to the map
        for (Group y : LuckPermsManager.getGroups()){
            if (y.getWeight().isPresent()){
                groupWeight.put(y.getName(), y.getWeight().getAsInt());
            }
            else{
                Bukkit.getLogger().severe("LuckPerms group " + y.getName() + " doesn't have any weight! CoreSettings the weight to 1...");

                Objects.requireNonNull(LuckPermsManager.getGroupManager().getGroup(y.getName()),
                        "Group " + y.getName() + " wasn't found when setting a missing weight")
                        .data().add(Node.builder("weight.1").build());

                LuckPermsManager.getGroupManager().saveGroup(y);
                groupWeight.put(y.getName(), y.getWeight().getAsInt());
            }
        }

        /*
         order the map by highest value and put every group to a new ordered map with "i" value.
         also register a new team with (i + name). The lower "i", the highest order priority.
         Example: 1prime is lower then 0admin
        */
        for (String x : groupWeight.entrySet()
                .stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey).toList()){
            groupOrder.put(x, i);

            Group group = LuckPermsManager.getGroupManager().getGroup(x);
            if (group == null) return;

            Team team = playerListScoreboard.registerNewTeam(i + "" + x);
            Component prefix = LuckPermsManager.getGroupPrefix(group);
            if (!x.equals("default"))
                team.prefix(prefix.append(Component.text(" ")));
            i++;
        }
    }

    /** Add player to the corresponding team by getting his primary group and its group's weight.
    set the prefix to team with luckperms cached data.
     * @param uuid UUID of whom to add to the scoreboard team
     */
    public void addToTeam(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        playerListScoreboard = core.getPlayerListScoreboard();
        String stringTeam = groupOrder.get(LuckPermsManager.getPlayerPrimaryGroup(uuid)) + LuckPermsManager.getPlayerPrimaryGroup(uuid);
        Team team = playerListScoreboard.getTeam(stringTeam);

        if (team == null){
            Bukkit.getLogger().severe("Scoreboard team " + stringTeam + " wasn't found");
            return;
        }

        team.addPlayer(player);

        if (!stringTeam.contains("default"))
            team.prefix(LuckPermsManager.getPlayerPrefix(uuid).append(Component.text(" ")));

        player.setScoreboard(playerListScoreboard);
    }

    /**
     * @param uuid UUID of whom to remove from the scoreboard team
      */
    public void removeFromTeam(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        playerListScoreboard = core.getPlayerListScoreboard();
        Team team = playerListScoreboard.getPlayerTeam(player);
        if (team != null)
                team.removePlayer(player);
    }
}
