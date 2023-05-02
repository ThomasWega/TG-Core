package net.trustgames.core.tablist;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class TablistHandler implements Listener {

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        TablistTeams.addPlayer(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        TablistTeams.removePlayer(event.getPlayer());
    }
}
