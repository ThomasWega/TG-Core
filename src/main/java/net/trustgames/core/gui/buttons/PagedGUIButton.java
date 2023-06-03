package net.trustgames.core.gui.buttons;

import lombok.Getter;
import lombok.Setter;
import net.trustgames.core.gui.type.PaginatedGUI;
import net.trustgames.core.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * GUIButton with extended functionality of switching pages
 * and the option to replace the button with GUIButton in case
 * the Page Switching is not needed (ex. previous page button on first page)
 */
public class PagedGUIButton extends GUIButton implements Cloneable {

    @Getter
    @Setter
    private Function<PaginatedGUI, SwitchAction> pageManager;

    @Getter
    @Setter
    private Function<PaginatedGUI, GUIButton> replaceManager;

    /**
     * Set the ItemStack or the Meta for the Item
     *
     * @param iconCreator the icon creator
     * @return the inventory page button
     */
    @Override
    public PagedGUIButton creator(Function<Player, ItemBuilder> iconCreator) {
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
    public PagedGUIButton event(Consumer<InventoryClickEvent> eventConsumer) {
        super.event(eventConsumer);
        return this;
    }


    /**
     * Set the SwitchAction to the button
     * (whether to switch the page to next or previous)
     *
     * @param pageManager the page manager
     * @return the inventory page button
     */
    public PagedGUIButton pager(Function<PaginatedGUI, SwitchAction> pageManager) {
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
    public PagedGUIButton replace(Function<PaginatedGUI, @Nullable GUIButton> replaceManager) {
        this.replaceManager = replaceManager;
        return this;
    }

    /**
     * Uses the clone of GUIButton, but also
     * clones the Page and Replace Managers
     *
     * @return Deep clone of the PagedGUIButton
     * @see GUIButton#clone()
     */
    @Override
    public PagedGUIButton clone() {
        PagedGUIButton clone = (PagedGUIButton) super.clone();
        clone.pageManager = this.pageManager;
        clone.replaceManager = this.replaceManager;
        return clone;
    }

    public enum SwitchAction {
        NEXT,
        PREVIOUS
    }
}
