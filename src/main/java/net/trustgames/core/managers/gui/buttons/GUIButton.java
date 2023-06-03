package net.trustgames.core.managers.gui.buttons;

import lombok.Getter;
import lombok.Setter;
import net.trustgames.core.managers.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Create an item for the GUI and specify the Material/Meta and what happens on
 * click on the item
 */
public class GUIButton implements Cloneable {

    @Getter
    @Setter
    private Function<Player, ItemBuilder> iconCreator;

    @Getter
    @Setter
    private Consumer<InventoryClickEvent> eventConsumer;

    @Getter
    @Setter
    private long updateIntervalTick = -1;


    /**
     * Set the ItemStack or the Meta for the Item
     *
     * @param iconCreator the icon creator
     * @return the inventory button
     */
    public GUIButton creator(Function<Player, ItemBuilder> iconCreator) {
        this.iconCreator = iconCreator;
        return this;
    }

    /**
     * Set what should happen on a click at the item
     *
     * @param eventConsumer the event consumer
     * @return the inventory button
     */
    public GUIButton event(Consumer<InventoryClickEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    /**
     * Creates a new GUIButton and sets
     * the IconCreator and EventConsumer to the original one
     *
     * @return Deep clone of the GUIButton
     */
    @Override
    public GUIButton clone() {
        GUIButton clone;
        try {
            clone = (GUIButton) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to create a clone of GUIButton", e);
        }
        clone.iconCreator = this.iconCreator;
        clone.eventConsumer = this.eventConsumer;
        return clone;
    }
}

