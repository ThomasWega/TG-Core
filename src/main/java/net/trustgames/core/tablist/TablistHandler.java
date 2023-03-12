package net.trustgames.core.tablist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.config.ServerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Handles the player-list creation
 */
public final class TablistHandler implements Listener {

    private final Core core;
    private final TablistTeams tablistTeams;

    public TablistHandler(Core core) {
        this.core = core;
        this.tablistTeams = new TablistTeams(core.getTablistScoreboard());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
            Component header = ServerConfig.TABLIST_HEADER.getText();
            Component footer = ServerConfig.TABLIST_FOOTER.getText();

            player.sendPlayerListHeaderAndFooter(header, footer);

            Scoreboard playerListScoreboard = core.getTablistScoreboard();
            tablistTeams.addToTeam(player);
            player.setScoreboard(playerListScoreboard);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        tablistTeams.removeFromTeam(event.getPlayer());
    }
}
