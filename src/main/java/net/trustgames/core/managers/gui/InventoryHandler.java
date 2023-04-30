package net.trustgames.core.managers.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * The interface Inventory handler.
 */
public interface InventoryHandler {

    /**
     * On click.
     *
     * @param event the event
     */
    void onClick(InventoryClickEvent event);

    /**
     * On open.
     *
     * @param event the event
     */
    void onOpen(InventoryOpenEvent event);

    /**
     * On close.
     *
     * @param event the event
     */
    void onClose(InventoryCloseEvent event);

}
