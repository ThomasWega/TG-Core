package net.trustgames.core.managers.gui.buttons;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * GUIButton with specific event for hotbar click
 */
public class HotbarGUIButton extends GUIButton implements Cloneable {

    @Getter
    @Setter
    private Consumer<PlayerInteractEvent> eventConsumerHotbar;

    /**
     * Set the ItemStack or the Meta for the Item
     *
     * @param iconCreator the icon creator
     * @return the inventory button
     */
    @Override
    public HotbarGUIButton creator(Function<Player, ItemStack> iconCreator) {
        super.creator(iconCreator);
        return this;
    }

    /**
     * Set what should happen on a click at the item
     *
     * @param eventConsumer the event consumer
     * @return the inventory button
     */
    @Override
    public HotbarGUIButton event(Consumer<InventoryClickEvent> eventConsumer) {
        super.event(eventConsumer);
        return this;
    }

    /**
     * Set what should happen on a click at the item when in hotbar
     * (inventory not open)
     *
     * @param eventConsumer the event consumer
     * @return the inventory button
     */
    public HotbarGUIButton eventHotbar(Consumer<PlayerInteractEvent> eventConsumer) {
        this.eventConsumerHotbar = eventConsumer;
        return this;
    }

    /**
     * Uses the clone of GUIButton, but also
     * clones the Hotbar Consumer
     *
     * @return Deep clone of the HotbarGUIButton
     * @see GUIButton#clone()
     */
    @Override
    public HotbarGUIButton clone() {
        HotbarGUIButton clone = (HotbarGUIButton) super.clone();
        clone.eventConsumerHotbar = this.eventConsumerHotbar;
        return clone;
    }
}
