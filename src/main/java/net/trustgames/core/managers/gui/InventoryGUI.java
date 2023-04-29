package net.trustgames.core.managers.gui;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.trustgames.core.managers.gui.buttons.InventoryButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single page chest inventory
 */
public abstract class InventoryGUI implements InventoryHandler, Cloneable {

    private final List<Integer> inventorySizes = Arrays.asList(9, 18, 27, 36, 36, 46, 54);
    protected Map<Integer, @Nullable InventoryButton> buttonMap = new HashMap<>();
    @Getter
    private Inventory inventory;
    @Getter
    @Setter
    private Component inventoryTitle;

    public InventoryGUI(@NotNull Component inventoryTitle,
                        int inventorySize) {
        this.inventory = Bukkit.createInventory(
                null,
                getClosestSize(inventorySize),
                inventoryTitle);
        this.inventoryTitle = inventoryTitle;
    }

    /**
     * @param size Supplied size
     * @return The closest higher number of max inventory slots
     */
    private int getClosestSize(int size) {
        for (int i : inventorySizes) {
            if (size <= i) {
                return i;
            }
        }
        throw new IndexOutOfBoundsException("Inventory size of " + size +
                "is exceeding the Minecraft limit of 54");
    }

    /**
     * @param slot   Slot to set the button at
     * @param button Button to be set
     */
    public void setButton(int slot, InventoryButton button) {
        buttonMap.put(slot, button);
    }

    /**
     * @param slot Slot to get the button at
     * @return Button at the given slot
     */
    public @Nullable InventoryButton getButton(int slot) {
        return buttonMap.get(slot);
    }

    /**
     * Sets each of not-null set buttons to the inventory
     *
     * @param player Player to decorate the inventory for
     */
    public void decorate(Player player) {
        buttonMap.forEach((slot, button) -> {
            ItemStack icon = button != null ? button.getIconCreator().apply(player) : null;
            inventory.setItem(slot, icon);
        });
    }


    /**
     * If the button isn't null and has set event,
     * handle the execution of it.
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        InventoryButton button = buttonMap.get(slot);
        if (button != null && button.getEventConsumer() != null) {
            button.getEventConsumer().accept(event);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.decorate((Player) event.getPlayer());
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
    }

    /**
     * First it creates a new Bukkit inventory with the same size and title.
     * Then it moves all the items to the inventory.
     * Lastly it copies all the buttons and their index to
     * a new cloned map of buttons
     *
     * @return deep clone of the InventoryGUI
     */
    @Override
    public InventoryGUI clone() {
        try {
            InventoryGUI cloned = (InventoryGUI) super.clone();
            cloned.inventory = Bukkit.createInventory(null, inventory.getSize(), inventoryTitle);
            // copy the items to the new inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    cloned.inventory.setItem(i, item.clone());
                }
            }
            // copy the buttons to the new inventory
            cloned.buttonMap = new HashMap<>();
            buttonMap.forEach((integer, inventoryButton) -> {
                if (inventoryButton != null) {
                    cloned.buttonMap.put(integer, inventoryButton.clone());
                } else {
                    cloned.buttonMap.put(integer, null);
                }
            });
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
