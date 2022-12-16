package net.trustgames.core.inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HotbarListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // get the other methods
        HotbarItems.addFromItemList(player);

        // hide players
        HotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // hide players
        HotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }

    // cancel itemclick event
    @EventHandler
    public void itemClickEvent(InventoryClickEvent event) {
            event.setCancelled(true);
    }

    // cancel item drag event
    @EventHandler
    public void itemDragEvent(InventoryDragEvent event){
            event.setCancelled(true);
    }

    // cancel item drop event
    @EventHandler
    public void itemDropEvent(PlayerDropItemEvent event) {
            event.setCancelled(true);
    }
}
