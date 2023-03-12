package net.trustgames.core.tablist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
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
    private final UUIDCache uuidCache;

    public TablistHandler(Core core) {
        this.core = core;
        this.tablistTeams = new TablistTeams(core.getTablistScoreboard());
        this.uuidCache = core.getUuidCache();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        uuidCache.get(player.getName(), uuid -> {
            Component header = ServerConfig.TABLIST_HEADER.getText();
            Component footer = ServerConfig.TABLIST_FOOTER.getText();

            player.sendPlayerListHeaderAndFooter(header, footer);

            Scoreboard playerListScoreboard = core.getTablistScoreboard();
            tablistTeams.addToTeam(uuid);
            player.setScoreboard(playerListScoreboard);
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        uuidCache.get(event.getPlayer().getName(), tablistTeams::removeFromTeam);
    }
}
