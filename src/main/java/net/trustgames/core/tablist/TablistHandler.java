package net.trustgames.core.tablist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.config.ServerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * Handles the player-list creation
 */
public class TablistHandler implements Listener {

    private final Core core;
    private TablistTeams tablistTeams;

    public TablistHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);

        Component header = ServerConfig.TABLIST_HEADER.getText();
        Component footer = ServerConfig.TABLIST_FOOTER.getText();

        player.sendPlayerListHeaderAndFooter(header, footer);

        Scoreboard playerListScoreboard = core.getTablistScoreboard();
        tablistTeams = new TablistTeams(core);
        tablistTeams.addToTeam(uuid);
        player.setScoreboard(playerListScoreboard);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = EntityCache.getUUID(event.getPlayer());

        tablistTeams = new TablistTeams(core);
        tablistTeams.removeFromTeam(uuid);
    }
}
