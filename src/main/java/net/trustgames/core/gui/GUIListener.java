package net.trustgames.core.gui;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(Core core) {
        this.guiManager = core.getGuiManager();
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        guiManager.handleClick(event);
    }

    @EventHandler
    private void onOpen(InventoryOpenEvent event) {
        guiManager.handleOpen(event);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        guiManager.handleClose(event);
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        guiManager.handleInteract(event);
    }

    @EventHandler
    private void onHotbarDrop(PlayerDropItemEvent event) {
        guiManager.handleHotbarDrop(event);
    }
}
