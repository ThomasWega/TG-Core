package net.trustgames.core.inventories;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class HotbarListeners implements Listener {

    private final Core core;

    public HotbarListeners(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // get the other methods
        HotbarItems hotbarItems = new HotbarItems();
        hotbarItems.addFromItemList(player);

        // hide players
        hotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // get methods
        HotbarItems hotbarItems = new HotbarItems();

        // hide players
        hotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }

    /*
    cancel inventory item movement events
    check if the server-type in config.yml is set to lobby
    minigames might have different settings and might want to allow the item movements and drops
     */

    public boolean isLobby() {
        return Objects.requireNonNull(core.getConfig().getString("settings.server-type")).equalsIgnoreCase("LOBBY");
    }

    // cancel itemclick event
    @EventHandler
    public void itemClickEvent(InventoryClickEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    // cancel item drag event
    @EventHandler
    public void itemDragEvent(InventoryDragEvent event){
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    // cancel item drop event
    @EventHandler
    public void itemDropEvent(PlayerDropItemEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }
}
