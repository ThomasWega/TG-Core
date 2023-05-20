package net.trustgames.core.managers.gui.player;

import net.trustgames.core.managers.gui.GUIManager;
import net.trustgames.core.managers.gui.InventoryGUI;
import net.trustgames.core.managers.gui.buttons.GUIButton;
import net.trustgames.core.managers.gui.buttons.HotbarGUIButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PlayerGUI implements PlayerInventoryHandler {

    private Map<Integer, HotbarGUIButton> buttonMap = new HashMap<>();
    private final Player player;

    /**
     * Manage player's personal inventory including hotbar, with InventoryButtons.
     * Allows to have different actions when the item is clicked
     * from open inventory or from hotbar
     *
     * @see InventoryGUI
     */
    public PlayerGUI(GUIManager guiManager, @NotNull Player player) {
        this.player = player;
        guiManager.registerInventory(player.getInventory(), this);
    }

    /**
     * Clears the whole inventory and sets the given buttons on specified slots
     *
     * @param buttons Collection of buttons to make the new inventory content out of
     */
    public void setContents(@NotNull Map<Integer, HotbarGUIButton> buttons) {
        this.buttonMap = buttons;
        player.getInventory().clear();

        buttons.forEach((slot, button) -> {
            ItemStack icon = button != null ? button.getIconCreator().apply(player) : null;
            player.getInventory().setItem(slot, icon);
        });
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
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        int slot = Arrays.stream(event.getPlayer().getInventory().getContents()).toList().indexOf(item);
        if (slot == -1) return;

        event.setCancelled(true);
        HotbarGUIButton button = buttonMap.get(slot);
        if (button != null && button.getEventConsumer() != null) {
            button.getEventConsumerHotbar().accept(event);
        }
    }

    @Override
    public void onHotbarItemDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
