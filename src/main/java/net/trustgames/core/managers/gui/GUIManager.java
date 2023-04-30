package net.trustgames.core.managers.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

/**
 * The type Gui manager.
 */
public class GUIManager {

    private final HashMap<Inventory, InventoryHandler> activeInventories = new HashMap<>();

    /**
     * Register the GUI's inventory and then open it for the player
     *
     * @param player Player to open the GUI for
     * @param gui    What GUI to open
     */
    public void openInventory(Player player, InventoryGUI gui) {
        Inventory inv = gui.getInventory();
        registerInventory(inv, gui);
        player.openInventory(inv);
    }

    /**
     * Add the inventory to the list of active inventories to manage
     *
     * @param inventory Inventory to be added
     * @param handler   the handler
     */
    public void registerInventory(Inventory inventory, InventoryHandler handler) {
        activeInventories.put(inventory, handler);
    }

    /**
     * Remove the inventory from the list of active inventories to manage
     *
     * @param inventory Inventory to be removed
     */
    public void unregisterInventory(Inventory inventory) {
        activeInventories.remove(inventory);
    }

    /**
     * Handle click.
     *
     * @param event the event
     */
    public void handleClick(InventoryClickEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onClick(event);
        }
    }

    /**
     * Handle open.
     *
     * @param event the event
     */
    public void handleOpen(InventoryOpenEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onOpen(event);
        }
    }


    /**
     * Handle close.
     *
     * @param event the event
     */
    public void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = activeInventories.get(inventory);
        if (handler != null) {
            handler.onClose(event);
            unregisterInventory(inventory);
        }
    }

}

