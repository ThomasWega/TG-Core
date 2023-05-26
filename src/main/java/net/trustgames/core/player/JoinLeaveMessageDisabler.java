package net.trustgames.core.player;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveMessageDisabler implements Listener {

    public JoinLeaveMessageDisabler(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerJoin(PlayerJoinEvent event){
        event.joinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event){
        event.quitMessage(null);
    }
}
