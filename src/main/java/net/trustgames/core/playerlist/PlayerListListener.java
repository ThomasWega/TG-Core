package net.trustgames.core.playerlist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Handles the player-list creation
 */
public class PlayerListListener implements Listener {

    private final Core core;

    public PlayerListListener(Core core) {
        this.core = core;
    }

    PlayerListTeams playerListTeams;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        FileConfiguration config = core.getConfig();

        player.sendPlayerListHeader(Component.text(ChatColor.translateAlternateColorCodes('&', String.join("\n", config.getStringList("tablist.header")))));
        player.sendPlayerListFooter(Component.text(ChatColor.translateAlternateColorCodes('&', String.join("\n", config.getStringList("tablist.footer")))));

        Scoreboard playerListScoreboard = core.getPlayerListScoreboard();
        playerListTeams = new PlayerListTeams(core);
        playerListTeams.addToTeam(player);
        player.setScoreboard(playerListScoreboard);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        playerListTeams = new PlayerListTeams(core);
        PlayerListTeams.removeFromTeam(player);
    }
}
