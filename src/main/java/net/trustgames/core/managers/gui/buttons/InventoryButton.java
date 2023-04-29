package net.trustgames.core.managers.gui.buttons;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Create an item for the GUI and specify the Material/Meta and what happens on
 * click on the item
 */
public class InventoryButton implements Cloneable {

    @Getter
    @Setter
    private Function<Player, ItemStack> iconCreator;

    @Getter
    @Setter
    private Consumer<InventoryClickEvent> eventConsumer;

    /**
     * Set the ItemStack or the Meta for the Item
     */
    public InventoryButton creator(Function<Player, ItemStack> iconCreator) {
        this.iconCreator = iconCreator;
        return this;
    }

    /**
     * Set what should happen on a click at the item
     */
    public InventoryButton consumer(Consumer<InventoryClickEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    /**
     * Creates a new InventoryButton and sets
     * the IconCreator and EventConsumer to the original one
     *
     * @return Deep clone of the InventoryButton
     */
    @Override
    public InventoryButton clone() {
        InventoryButton clone;
        try {
            clone = (InventoryButton) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.iconCreator = this.iconCreator;
        clone.eventConsumer = this.eventConsumer;
        return clone;
    }
}

