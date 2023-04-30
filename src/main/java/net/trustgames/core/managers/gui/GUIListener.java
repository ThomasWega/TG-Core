package net.trustgames.core.managers.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * The type Gui listener.
 */
public class GUIListener implements Listener {

    private final GUIManager guiManager;

    /**
     * Instantiates a new Gui listener.
     *
     * @param guiManager the gui manager
     */
    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * On click.
     *
     * @param event the event
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        guiManager.handleClick(event);
    }

    /**
     * On open.
     *
     * @param event the event
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        guiManager.handleOpen(event);
    }

    /**
     * On close.
     *
     * @param event the event
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        guiManager.handleClose(event);
    }
}
