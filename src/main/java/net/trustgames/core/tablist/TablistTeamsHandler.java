package net.trustgames.core.tablist;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class TablistTeamsHandler implements Listener {

    public TablistTeamsHandler(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        TablistTeams.addPlayer(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        TablistTeams.removePlayer(event.getPlayer());
    }
}
