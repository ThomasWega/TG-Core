package net.trustgames.core.gui;

import net.trustgames.core.Core;
import net.trustgames.core.gui.type.InventoryGUI;
import net.trustgames.core.gui.type.InventoryHandler;
import net.trustgames.core.gui.type.PlayerInventoryHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class GUIManager {

    private final Core core;
    private final HashMap<Inventory, InventoryHandler> activeInventories = new HashMap<>();

    public GUIManager(Core core) {
        this.core = core;
    }

    /**
     * Register the GUI's inventory and then open it for the player
     *
     * @param player Player to open the GUI for
     * @param gui    What GUI to open
     */
    public void openInventory(Player player, @NotNull InventoryGUI gui) {
        Inventory inv = gui.getInventory();
        registerInventory(inv, gui);
        Bukkit.getScheduler().runTask(core, () -> player.openInventory(inv));
    }

    /**
     * Add the inventory to the list of active inventories to manage
     *
     * @param inventory Inventory to be added
     * @param handler   the handler
     */
    public void registerInventory(@NotNull Inventory inventory,
                                  @NotNull InventoryHandler handler) {
        activeInventories.put(inventory, handler);
    }

    /**
     * Remove the inventory from the list of active inventories to manage
     *
     * @param inventory Inventory to be removed
     */
    public void unregisterInventory(@NotNull Inventory inventory) {
        activeInventories.remove(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;

        InventoryHandler handler = activeInventories.get(inventory);
        if (handler != null) {
            handler.onClick(event);
        }
    }

    public void handleOpen(InventoryOpenEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onOpen(event);
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = activeInventories.get(inventory);
        if (handler != null) {
            handler.onClose(event);
            unregisterInventory(inventory);
        }
    }

    /**
     * This event is called when item is clicked on from the hotbar
     * (not from an open inventory)
     */
    public void handleInteract(PlayerInteractEvent event){
        Inventory inventory = event.getPlayer().getInventory();
        InventoryHandler handler = activeInventories.get(inventory);
        if (handler instanceof PlayerInventoryHandler playerInventoryHandler) {
            playerInventoryHandler.onHotbarInteract(event);
        }
    }

    /**
     * This event is called when item is dropped from the hotbar
     * (not from an open inventory)
     */
    public void handleHotbarDrop(PlayerDropItemEvent event){
        Inventory inventory = event.getPlayer().getInventory();
        InventoryHandler handler = activeInventories.get(inventory);
        if (handler instanceof PlayerInventoryHandler playerInventoryHandler) {
            playerInventoryHandler.onHotbarItemDrop(event);
        }
    }
}

