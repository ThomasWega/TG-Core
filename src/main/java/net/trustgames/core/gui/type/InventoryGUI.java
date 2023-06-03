package net.trustgames.core.gui.type;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.trustgames.core.gui.buttons.GUIButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Single page chest inventory
 */
public class InventoryGUI implements InventoryHandler, Cloneable {

    protected Map<Integer, @Nullable GUIButton> buttonMap = new HashMap<>();

    @Getter
    private Inventory inventory;

    @Getter
    private Component inventoryTitle;

    @Getter
    private int inventorySize;

    /**
     * @param inventoryTitle the inventory title (can be changed later using setter)
     * @param rows           the number of rows the inventory should have (can be changed later using setter)
     */
    public InventoryGUI(@NotNull Component inventoryTitle,
                        Rows rows) {
        this.inventoryTitle = inventoryTitle;
        this.inventorySize = rows.slots;
        this.inventory = Bukkit.createInventory(
                null,
                inventorySize,
                inventoryTitle);
    }

    /**
     * Also updates the Inventory,
     * but it might need to be reopened for the player
     *
     * @param title What title to set the new Inventory to
     */
    public void setInventoryTitle(Component title) {
        this.inventoryTitle = title;
        this.inventory = Bukkit.createInventory(
                this.inventory.getHolder(),
                this.inventory.getSize(),
                this.inventoryTitle
        );
    }

    /**
     * Also updates the Inventory,
     * but it might need to be reopened for the player
     *
     * @param rows How many rows to set the new inventory to
     */
    public void setInventorySize(Rows rows) {
        this.inventorySize = rows.slots;
        this.inventory = Bukkit.createInventory(
                inventory.getHolder(),
                this.inventorySize,
                this.inventoryTitle
        );
    }

    /**
     * Adds a button to the inventory. If the inventory is full,
     * the button won't be added
     *
     * @param button Button to be added
     * @return true - if button was added
     * <p>false - if the button couldn't be added (inventory is full)
     * @see InventoryGUI#addAll(Collection)
     * @deprecated This method is currently untested and may not function correctly.
     */
    public boolean addButton(GUIButton button) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (buttonMap.get(i) == null) {
                buttonMap.put(i, button);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds multiple buttons to the inventory
     *
     * @param buttons Buttons to be added to the inventory
     * @return List of buttons which weren't added
     * @see InventoryGUI#addButton(GUIButton)
     * @deprecated This method is currently untested and may not function correctly.
     */
    public List<GUIButton> addAll(Collection<GUIButton> buttons) {
        List<GUIButton> unAdded = new ArrayList<>();
        buttons.forEach(button -> {
            boolean addSuccess = addButton(button);
            if (!addSuccess) {
                unAdded.add(button);
            }
        });
        return unAdded;
    }

    /**
     * Sets button at the specified slot in the inventory.
     * Will override any existing buttons
     *
     * @param slot   Slot to set the button at
     * @param button Button to be set
     */
    public void setButton(int slot, GUIButton button) {
        buttonMap.put(slot, button);
    }

    /**
     * Gets the button at the specified slot
     *
     * @param slot Slot to get the button at
     * @return Button at the given slot
     */
    public Optional<GUIButton> getButton(int slot) {
        return Optional.ofNullable(buttonMap.get(slot));
    }

    /**
     * Sets each of not-null set buttons to the inventory
     *
     * @param player Player to decorate the inventory for
     */
    public void decorate(Player player) {
        buttonMap.forEach((slot, button) -> {
            ItemStack icon = button != null ? button.getIconCreator().apply(player).build() : null;
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
        GUIButton button = buttonMap.get(slot);
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

    public enum Rows {
        ONE(9),
        TWO(18),
        THREE(27),
        FOUR(36),
        FIVE(45),
        SIX(54);

        @Getter
        private final int slots;

        Rows(int slots) {
            this.slots = slots;
        }
    }
}
