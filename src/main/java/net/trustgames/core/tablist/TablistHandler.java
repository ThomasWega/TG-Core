package net.trustgames.core.tablist;

import net.kyori.adventure.text.Component;
import net.trustgames.core.config.ServerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles the player-list creation
 */
public final class TablistHandler implements Listener {

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
            Component header = ServerConfig.TABLIST_HEADER.getText();
            Component footer = ServerConfig.TABLIST_FOOTER.getText();

            player.sendPlayerListHeaderAndFooter(header, footer);

            TablistTeams.addToTeam(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        TablistTeams.removeFromTeam(event.getPlayer());
    }
}
