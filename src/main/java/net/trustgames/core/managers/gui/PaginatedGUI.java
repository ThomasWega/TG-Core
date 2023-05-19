package net.trustgames.core.managers.gui;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.managers.gui.buttons.GUIButton;
import net.trustgames.core.managers.gui.buttons.PagedGUIButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class PaginatedGUI extends InventoryGUI {

    private final GUIManager guiManager;
    private final List<InventoryGUI> pages = new ArrayList<>();

    private final ConcurrentHashMap<UUID, Integer> currentPage = new ConcurrentHashMap<>();

    /**
     * Manage a list of InventoryGUIs as pages and handle switching of these pages
     *
     * @see InventoryGUI
     */
    public PaginatedGUI(@NotNull GUIManager guiManager,
                        @NotNull Component title,
                        @NotNull Rows rows) {
        super(title, rows);
        this.guiManager = guiManager;
    }

    /**
     * Sorts the list of buttons into pages (inventories)
     * with the size from the constructor and with the template gui.
     * <p></p>
     * First it calculates the amount of free slots there is on every page
     * and then the amount of pages needed to fill in all the buttons and then creates
     * that amount of copies of the template gui.
     * <p></p>
     * At this stage, it is also
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
    @SuppressWarnings("CommentedOutCode")
    public void paginate(List<GUIButton> buttons) {
        this.pages.clear();
        int invSize = this.getInventory().getSize();

        // how many free (undefined) slots there is in the templateGui
        List<Integer> freeSlots = IntStream.range(0, invSize)
                .filter(i -> !this.buttonMap.containsKey(i))
                .boxed()
                .toList();

        int pageAmount = (int) Math.ceil((double) buttons.size() / freeSlots.size());

        // remove page buttons on first and last page
        for (int i = 0; i < pageAmount; i++) {
            InventoryGUI templateGuiClone = this.clone();
            if (i == 0) {
                onFirstPagedButtons(templateGuiClone);
            }
            if (i == pageAmount - 1) {
                onLastPagedButtons(templateGuiClone);
            }
            pages.add(templateGuiClone);
        }

        fillPages(invSize, buttons);

        /*
         // a different approach to paginating, which could remove the
         // loop above and the fillPages method, but this is UNTESTED!!
         // and not recommended (at least without proper testing)

        InventoryGUI tGuiClone = templateGui.clone();
        int pageCount = 0;
        for (int i = 0; i < buttons.size(); i++) {
            boolean added = tGuiClone.addButton(buttons.get(i));
            if (added) return;

            if (pageCount == 0) {
                onFirstPagedButtons(tGuiClone);
            } else if (pageCount == pageAmount - 1){
                onLastPagedButtons(tGuiClone);
            }
            pages.add(tGuiClone);
            tGuiClone = templateGui.clone();
            pageCount++;
            i--;
        }
         */
    }

    /**
     * Replace the previous page button on the first page
     * with its defined replacement
     *
     * @param templateGuiClone InventoryGUI where to replace the button
     */
    private void onFirstPagedButtons(InventoryGUI templateGuiClone) {
        templateGuiClone.buttonMap.forEach((slot, button) -> {
            if (!(button instanceof PagedGUIButton pageButton)) return;
            if (pageButton.getPageManager().apply(this) != PagedGUIButton.SwitchAction.PREVIOUS) return;

            templateGuiClone.buttonMap.replace(slot, pageButton.getReplaceManager().apply(this));
        });
    }

    /**
     * Replace the next page button on the first page
     * with its defined replacement
     *
     * @param templateGuiClone InventoryGUI where to replace the button
     */
    private void onLastPagedButtons(InventoryGUI templateGuiClone) {
        templateGuiClone.buttonMap.forEach((slot, button) -> {
            if (!(button instanceof PagedGUIButton pageButton)) return;
            if (pageButton.getPageManager().apply(this) != PagedGUIButton.SwitchAction.NEXT) return;

            templateGuiClone.buttonMap.replace(slot, pageButton.getReplaceManager().apply(this));
        });
    }

    /**
     * Loops through all the pages and fills each one with the given buttons
     *
     * @param invSize Size of the inventory
     * @param buttons Buttons to fill the inventory with
     */
    private void fillPages(int invSize, List<GUIButton> buttons) {
        int buttonIndex = 0;
        for (InventoryGUI inventoryGUI : pages) {
            for (int i = 0; i < invSize; i++) {
                if (buttonIndex >= buttons.size()) {
                    break;
                }

                if (inventoryGUI.getButton(i).isEmpty()) {
                    inventoryGUI.setButton(i, buttons.get(buttonIndex));
                    buttonIndex++;
                }
            }
        }
    }

    /**
     * Open the GUI at the specified page
     *
     * @param player Player to open the GUI for
     * @param index  What page to open the GUI at (starting at 0)
     */
    public void openPage(Player player, int index) {
        UUID uuid = player.getUniqueId();
        currentPage.put(uuid, index);
        this.guiManager.openInventory(player, pages.get(currentPage.get(uuid)));
    }

    public void openCurrentPage(Player player) {
        UUID uuid = player.getUniqueId();
        Optional<InventoryGUI> page = getCurrentPage(uuid);
        if (page.isEmpty()) {
            Core.LOGGER.warning("No page is present for uuid " + uuid);
            return;
        }

        this.guiManager.openInventory(player, page.get());
    }

    /**
     * Open the GUI at the first page
     *
     * @param player Player to open the GUI for
     */
    public void openFirstPage(Player player) {
        this.openPage(player, 0);
    }


    /**
     * Open the GUI at the last page
     *
     * @param player Player to open the GUI for
     */
    public void openLastPage(Player player) {
        this.openPage(player, getPagesAmount() - 1);
    }

    /**
     * Switches the gui page to the next one
     *
     * @param player Player to switch the GUI for
     * @see PaginatedGUI#openPreviousPage(Player) PaginatedGUI#previousPage(Player)
     */
    public void openNextPage(Player player) {
        openPage(player, currentPage.get(player.getUniqueId()) + 1);
    }

    /**
     * Switches the gui page to the previous one
     *
     * @param player Player to switch the GUI for
     * @see PaginatedGUI#openNextPage(Player) PaginatedGUI#nextPage(Player)
     */
    public void openPreviousPage(Player player) {
        openPage(player, currentPage.get(player.getUniqueId()) - 1);
    }

    /**
     * @return first InventoryGUI page
     */
    public InventoryGUI getFirstPage() {
        return pages.get(0);
    }

    /**
     * @return last InventoryGUI page
     */
    public InventoryGUI getLastPage() {
        return pages.get(pages.size() - 1);
    }

    /**
     * Gets current page the player is
     *
     * @param uuid UUID of the player to get the page for
     * @return The int of page the GUI is currently set at (starting at 0)
     */
    public Optional<InventoryGUI> getCurrentPage(UUID uuid) {
        Integer page = currentPage.get(uuid);
        if (page == null)
            return Optional.empty();


        return Optional.of(pages.get(page));
    }

    /**
     * Gets page at the specified index
     *
     * @param index The page number (starting from 0)
     * @return InventoryGUI located at the specified page
     */
    public InventoryGUI getPage(int index) {
        return pages.get(index);
    }

    /**
     * Gets the page the player is at
     *
     * @param uuid The UUID of the player to check for
     * @return InventoryGUI that the player has opened right now
     */
    public @Nullable InventoryGUI getPage(UUID uuid) {
        return pages.get(currentPage.get(uuid));
    }

    /**
     * @param uuid The UUID of the player to get the index for
     * @return Integer index of the page the player is currently at
     */
    public int getPageIndex(UUID uuid) {
        Integer page = currentPage.get(uuid);
        return Objects.requireNonNullElse(page, 0);
    }

    /**
     * @return The amount of pages the GUI has
     */
    public int getPagesAmount() {
        return pages.size();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);

        /*
        if the player opens the next or previous page, the reason will be
        OPEN_NEW, so there is no need to do anything. However, when the reason is different,
        that probably means the player closed as a whole (not only the page) and therefore
        he should be removed from the list
         */
        if (event.getReason() != InventoryCloseEvent.Reason.OPEN_NEW) {
            currentPage.remove(event.getPlayer().getUniqueId());
        }
    }
}
