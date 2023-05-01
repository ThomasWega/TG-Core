package net.trustgames.core.managers.gui.buttons;

import lombok.Getter;
import lombok.Setter;
import net.trustgames.core.managers.gui.PaginatedGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * InventoryButton with extended functionality of switching pages
 * and the option to replace the button with InventoryButton in case
 * the Page Switching is not needed (ex. previous page button on first page)
 */
public class InventoryPageButton extends InventoryButton implements Cloneable {

    @Getter
    @Setter
    private Function<PaginatedGUI, SwitchAction> pageManager;

    @Getter
    @Setter
    private Function<PaginatedGUI, InventoryButton> replaceManager;

    /**
     * Set the ItemStack or the Meta for the Item
     *
     * @param iconCreator the icon creator
     * @return the inventory page button
     */
    @Override
    public InventoryPageButton creator(Function<Player, ItemStack> iconCreator) {
        super.creator(iconCreator);
        return this;
    }

    /**
     * Set what should happen on a click at the item
     *
     * @param eventConsumer the event consumer
     * @return the inventory page button
     */
    @Override
    public InventoryPageButton consumer(Consumer<InventoryClickEvent> eventConsumer) {
        super.consumer(eventConsumer);
        return this;
    }


    /**
     * Set the SwitchAction to the button
     * (whether to switch the page to next or previous)
     *
     * @param pageManager the page manager
     * @return the inventory page button
     */
    public InventoryPageButton pager(Function<PaginatedGUI, SwitchAction> pageManager) {
        this.pageManager = pageManager;
        return this;
    }

    /**
     * Replacement button in case the page button needs to be removed
     * (on first or last page)
     *
     * @param replaceManager the replacement manager
     * @return the inventory page button
     * @see PaginatedGUI#paginate(List) PaginatedGUI#paginate(List)
     */
    public InventoryPageButton replace(Function<PaginatedGUI, @Nullable InventoryButton> replaceManager) {
        this.replaceManager = replaceManager;
        return this;
    }

    /**
     * Uses the clone of InventoryButton, but also
     * clones the Page and Replace Managers
     *
     * @return Deep clone of the InventoryPageButton
     * @see InventoryButton#clone()
     */
    @Override
    public InventoryPageButton clone() {
        InventoryPageButton clone = (InventoryPageButton) super.clone();
        clone.pageManager = this.pageManager;
        clone.replaceManager = this.replaceManager;
        return clone;
    }

    public enum SwitchAction {
        NEXT,
        PREVIOUS
    }
}
