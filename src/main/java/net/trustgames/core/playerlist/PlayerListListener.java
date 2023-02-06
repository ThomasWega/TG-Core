package net.trustgames.core.playerlist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.settings.CoreSettings;
import net.trustgames.core.utils.ColorUtils;
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
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        Component header = ColorUtils.color(CoreSettings.TABLIST_HEADER);
        Component footer = ColorUtils.color(CoreSettings.TABLIST_FOOTER);

        player.sendPlayerListHeaderAndFooter(header, footer);

        Scoreboard playerListScoreboard = core.getPlayerListScoreboard();
        playerListTeams = new PlayerListTeams(core);
        playerListTeams.addToTeam(player);
        player.setScoreboard(playerListScoreboard);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        playerListTeams = new PlayerListTeams(core);
        PlayerListTeams.removeFromTeam(player);
    }
}
