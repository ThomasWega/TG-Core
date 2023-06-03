package net.trustgames.core.gui.type;

import lombok.Getter;
import net.trustgames.core.gui.buttons.GUIButton;
import net.trustgames.core.gui.GUIManager;
import net.trustgames.core.gui.buttons.HotbarGUIButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PlayerGUI implements PlayerInventoryHandler {

    private Map<Integer, GUIButton> buttonMap;
    @Getter
    private final Player player;
    private final PlayerInventory playerInventory;

    /**
     * Manage player's personal inventory including hotbar, with InventoryButtons.
     * Allows to have different actions when the item is clicked
     * from open inventory or from hotbar
     *
     * @see InventoryGUI
     */
    public PlayerGUI(GUIManager guiManager, @NotNull Player player) {
        this.player = player;
        this.playerInventory = player.getInventory();
        guiManager.registerInventory(player.getInventory(), this);
    }

    /**
     * Clears the whole inventory and sets the given buttons on specified slots
     *
     * @param buttons Collection of buttons to make the new inventory content out of
     */
    public void setContents(@NotNull Map<Integer, GUIButton> buttons) {
        this.buttonMap = new HashMap<>(buttons);
        playerInventory.clear();
        decorate();
    }

    /**
     * Converts the buttons to itemStacks and sets them in the inventory
     */
    public void decorate() {
        buttonMap.forEach((slot, button) -> {
            ItemStack icon = button != null ? button.getIconCreator().apply(player).build() : null;
            playerInventory.setItem(slot, icon);
        });
    }

    /**
     * Sets the item on the slot to the buttons item
     *
     * @param slot Slot where to set the item
     * @param guiButton Button to set
     */
    public void setButton(int slot, @NotNull GUIButton guiButton) {
        buttonMap.put(slot, guiButton);
        playerInventory.setItem(slot, guiButton.getIconCreator().apply(player).build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        // the slots for PlayerInventory are inverted
        int slot = event.getRawSlot() - 36;
        GUIButton button = buttonMap.get(slot);
        if (button != null && button.getEventConsumer() != null) {
            button.getEventConsumer().accept(event);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public void onHotbarInteract(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) return;

        ItemStack item = event.getItem();
        int slot = Arrays.stream(event.getPlayer().getInventory().getContents()).toList().indexOf(item);
        if (slot == -1) return;

        event.setCancelled(true);
        GUIButton button = buttonMap.get(slot);
        if (button instanceof HotbarGUIButton hotbarGUIButton
                && hotbarGUIButton.getEventConsumerHotbar() != null) {
            hotbarGUIButton.getEventConsumerHotbar().accept(event);
        }
    }

    @Override
    public void onHotbarItemDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
