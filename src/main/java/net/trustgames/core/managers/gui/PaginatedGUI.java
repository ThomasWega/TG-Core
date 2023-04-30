package net.trustgames.core.managers.gui;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.trustgames.core.managers.gui.buttons.InventoryButton;
import net.trustgames.core.managers.gui.buttons.InventoryPageButton;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Type of InventoryGUI that manages pagination and switching of pages
 *
 * @see InventoryGUI
 */
public abstract class PaginatedGUI extends InventoryGUI {

    private final GUIManager guiManager;
    private final InventoryGUI templateGui;
    private final List<InventoryGUI> pages = new ArrayList<>();

    @Getter
    @Setter
    private int pageInt = 0;

    /**
     * Creates a new Paginated GUI
     *
     * @param guiManager     GUI Manager instance
     * @param inventoryTitle Title of the inventory (will also be used for copying)
     * @param inventorySize  Size of the inventory                       (the closest higher will be used if not exact)
     * @see InventoryGUI
     */
    public PaginatedGUI(@NotNull GUIManager guiManager,
                        @NotNull Component inventoryTitle,
                        int inventorySize) {
        super(inventoryTitle, inventorySize);
        this.guiManager = guiManager;
        this.templateGui = this.createTemplate();
    }

    /**
     * Sorts the list of buttons into pages (inventories)
     * with the size from the constructor and with the template gui.
     * <p></p>
     * First it calculates the amount of pages needed and then creates
     * that amount of copies of the template gui. At this stage, it is also
     * ensured that in each copy of the template gui there are correct InventoryPageButtons.
     * For example, on the first page, on the first page the previous page button
     * will be replaced by its replacement function.
     * The same goes for the next page button on the last page.
     * <p></p>
     * At last, the buttons from the list are added to the inventory's free slots
     * (slots not occupied by the buttons predefined in the template)
     *
     * @param buttons List of buttons to create pages for
     */
    public void paginate(List<InventoryButton> buttons) {
        int pageSize = templateGui.getInventory().getSize();
        int pageAmount = (int) Math.ceil((double) buttons.size() / pageSize);
        /*
         - If the page is the first one, make sure to use the replacement for
         the previous page button
         - If the page is the last one, make sure to use the replacement for the
         next page button

         For every page, create a new copy of template gui with the above changes
         */
        for (int i = 0; i < pageAmount; i++) {
            InventoryGUI templateGuiClone = templateGui.clone();
            // first page
            if (i == 0) {
                templateGuiClone.buttonMap.forEach((slot, button) -> {
                    if (button instanceof InventoryPageButton pageButton) {
                        if (pageButton.getPageManager().apply(this) == InventoryPageButton.SwitchAction.PREVIOUS) {
                            templateGuiClone.buttonMap.replace(slot, pageButton.getReplaceManager().apply(this));
                        }
                    }
                });
            }
            // last page
            if (i == pageAmount - 1) {
                templateGuiClone.buttonMap.forEach((slot, button) -> {
                    if (button instanceof InventoryPageButton pageButton) {
                        if (pageButton.getPageManager().apply(this) == InventoryPageButton.SwitchAction.NEXT) {
                            templateGuiClone.buttonMap.replace(slot, pageButton.getReplaceManager().apply(this));
                        }
                    }
                });
            }
            pages.add(templateGuiClone);
        }

        // loop through all the pages and fill each one with the given buttons
        int buttonIndex = 0;
        int pageCount = 0;
        for (InventoryGUI inventoryGUI : pages) {
            pageCount++;
            for (int i = 0; i < pageSize; i++) {
                if (buttonIndex >= buttons.size()) {
                    break;
                }
                InventoryButton button = inventoryGUI.getButton(i);
                if (button == null) {
                    inventoryGUI.setButton(i, buttons.get(buttonIndex));
                    buttonIndex++;
                }
            }
            if (buttonIndex >= buttons.size()) {
                break;
            }
            if (pageCount >= pages.size()) {
                pages.add(templateGui.clone());
            }

        }
    }

    /**
     * Open the GUI at the current (last) page.
     *
     * @param player Player to open the GUI for
     */
    public void openCurrentPage(Player player) {
        guiManager.openInventory(player, pages.get(pageInt));
    }

    /**
     * Open the GUI at the specified page
     *
     * @param player Player to open the GUI for
     * @param page   What page to open the GUI at
     */
    public void openPage(Player player, int page) {
        pageInt = page;
        guiManager.openInventory(player, getCurrentPage());
    }

    /**
     * Switches the gui page to the next one
     *
     * @param player Player to switch the GUI for
     * @see PaginatedGUI#previousPage(Player) PaginatedGUI#previousPage(Player)
     */
    public void nextPage(Player player) {
        openPage(player, ++pageInt);
    }

    /**
     * Switches the gui page to the previous one
     *
     * @param player Player to switch the GUI for
     * @see PaginatedGUI#nextPage(Player) PaginatedGUI#nextPage(Player)
     */
    public void previousPage(Player player) {
        openPage(player, --pageInt);
    }

    /**
     * Set the current page to the first page.
     *
     * @implNote The player inventory still needs to be reopened for this to take effect
     */
    public void setFirstPage() {
        pageInt = 0;
    }

    /**
     * Set the current page to the last page.
     *
     * @implNote The player inventory still needs to be reopened for this to take effect
     */
    public void setLastPage() {
        pageInt = getPagesAmount() - 1;
    }

    /**
     * @return first InventoryGUI page
     * @implNote might be null if the gui was not paginated yet
     */
    public Optional<InventoryGUI> getFirstPage() {
        return Optional.ofNullable(pages.get(0));
    }

    /**
     * @return last InventoryGUI page
     * @implNote might be null if the gui was not paginated yet
     */
    public Optional<InventoryGUI> getLastPage() {
        return Optional.ofNullable(pages.get(pages.size() - 1));
    }

    /**
     * Gets current page.
     *
     * @return The int of page the GUI is currently set at (starting at 0)
     */
    public InventoryGUI getCurrentPage() {
        return pages.get(pageInt);
    }

    /**
     * Gets page.
     *
     * @param page The page number (starting from 0)
     * @return InventoryGUI located at the specified page
     */
    public InventoryGUI getPage(int page) {
        return pages.get(page);
    }

    /**
     * Gets pages amount.
     *
     * @return The amount of pages there the GUI has
     */
    public int getPagesAmount() {
        return pages.size();
    }

    /**
     * Creates a gui that will be used as template for the pagination.
     * Every page will look as this templateGui, with the only exception that
     * InventoryPageButtons might get removed depending on what the current page is
     *
     * @return new InventoryGUI that will be used as a template
     * @see InventoryPageButton
     */
    protected abstract InventoryGUI createTemplate();
}
