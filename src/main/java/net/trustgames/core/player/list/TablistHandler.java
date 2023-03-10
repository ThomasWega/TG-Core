package net.trustgames.core.player.list;

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

import java.util.UUID;

/**
 * Handles the player-list creation
 */
public final class TablistHandler implements Listener {

    private final Core core;
    private TablistTeams tablistTeams;

    public TablistHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = UUIDCache.get(player.getName());

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
        UUID uuid = UUIDCache.get(event.getPlayer().getName());

        tablistTeams = new TablistTeams(core);
        tablistTeams.removeFromTeam(uuid);
    }
}
