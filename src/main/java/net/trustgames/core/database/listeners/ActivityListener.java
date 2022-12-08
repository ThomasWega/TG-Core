package net.trustgames.core.database.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ActivityListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // login date
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        // logout date
    }
}
