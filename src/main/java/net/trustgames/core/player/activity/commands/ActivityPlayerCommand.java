package net.trustgames.core.player.activity.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.trustgames.core.Core;
import net.trustgames.core.managers.InventoryManager;
import net.trustgames.core.managers.ItemManager;
import net.trustgames.core.player.activity.config.PlayerActivityType;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.cache.UUIDCache;
import net.trustgames.toolkit.config.CommandConfig;
import net.trustgames.toolkit.config.PermissionConfig;
import net.trustgames.toolkit.database.player.activity.PlayerActivity;
import net.trustgames.toolkit.database.player.activity.PlayerActivityFetcher;
import net.trustgames.toolkit.managers.HikariManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Opens up a menu with all the given target's logged activity.
 * The activity is separated in to pages and different actions
 * are differentiated by different Materials. The player can click
 * the item and all the data will be printed in chat.
 */
public final class ActivityPlayerCommand implements Listener {

    private final Core core;
    private final Toolkit toolkit;
    private final HikariManager hikariManager;
    private final PaperCommandManager<CommandSender> commandManager;

    /**
     * Stores all ItemStack with the data for each row
     */
    private static final List<ItemStack> records = new ArrayList<>();
    private static final List<Inventory> inventoryList = new ArrayList<>();

    private final Component nextPageName = ColorUtils.color("&eNext page");
    private final Component prevPageName = ColorUtils.color("&ePrevious page");
    /**
     * Used for switching pages.
     * +1 everytime page is switched to next page.
     * -1 everytime page is switched to previous page.
     */
    private int pageCount = 0;


    public ActivityPlayerCommand(Core core) {
        this.core = core;
        this.toolkit = core.getToolkit();
        this.hikariManager = toolkit.getHikariManager();
        this.commandManager = core.getCommandManager();
        register();
    }

    public void register() {

        // COMMAND
        cloud.commandframework.Command.Builder<CommandSender> activityCommand = commandManager.commandBuilder("activity",
                ArgumentDescription.of("ADD")
        );

        // TARGET argument
        CommandArgument<CommandSender, String> targetArg = StringArgument.<CommandSender>builder("target")
                .withSuggestionsProvider((context, s) -> Bukkit.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList())
                .asRequired()
                .build();

        commandManager.command(activityCommand
                .senderType(Player.class)
                .literal("player")
                .permission(PermissionConfig.STAFF.permission)
                .argument(targetArg)
                .handler(context -> {
                    Player player = ((Player) context.getSender());
                    if (hikariManager == null) {
                        player.sendMessage(CommandConfig.COMMAND_DATABASE_OFF.getText());
                        return;
                    }
                    String targetName = context.get(targetArg);

                    /*
        if the menus were previously opened, all the maps
        need to be cleared to avoid the items showing twice.
        The int needs to be reset so when the menu is opened a
        second time, the pages are reset to the first one
        */
                    records.clear();
                    inventoryList.clear();
                    pageCount = 0;

                    createRecords(targetName, () -> {
                        if (records.isEmpty()) {
                            player.sendMessage(CommandConfig.COMMAND_NO_PLAYER_DATA.addComponent(Component.text(targetName)));
                            return;
                        }

                        createPages(player, targetName);

                        // open the first inventory (first page) from the list on the main server thread
                        Bukkit.getScheduler().runTask(core, () -> player.openInventory(inventoryList.get(0)));
                    });
                })
        );
    }

    /**
     * Gets the ResultSet of offlinePlayer (target) from ActivityQuery
     * and for each result, it creates and ItemStack with display name
     * and lore with the corresponding data
     *
     * @param targetName Name of the targeted player
     */
    private void createRecords(@NotNull String targetName, Runnable callback) {
        UUIDCache uuidCache = new UUIDCache(toolkit, targetName);
        uuidCache.get(uuid -> {
            if (uuid == null) {
                callback.run();
                return;
            }
            PlayerActivityFetcher activityFetcher = new PlayerActivityFetcher(hikariManager);
            activityFetcher.fetchByUUID(uuid, playerActivity -> {
                if (playerActivity == null) {
                    callback.run();
                    return;
                }

                ItemStack targetHead = ItemManager.createItemStack(Material.PAINTING, 1);

            /*
             loop through all the results and for each one, set the corresponding
             id, uuid, ip, and time to the lore and set action as the display name.
             Also set the correct Material by using setMaterial method.
             Then add a clone of the ItemStack to the records list
             */
                for (PlayerActivity.Activity activity : playerActivity.getActivities()) {
                    long id = activity.getId();
                    String stringUuid = activity.getUuid().toString();
                    String ip = activity.getIp();
                    String action = activity.getAction();
                    Timestamp time = activity.getTime();

                    /*
                    One of the lore lines needs to have a click event with the value of the ID. This is
                    because in other methods it's required to retrieve the ID by just clicking on the item.
                    The click event is technically never being executed, as the player doesn't click on the lore, but
                    on the ItemStack, but the click event value is still present in the ItemStack's lore and can be retrieved.
                    That's how I get the ID from the ItemStack later on.
                    */
                    List<Component> loreList = new ArrayList<>();
                    loreList.add(Component.text(ChatColor.WHITE + "Date: " + ChatColor.YELLOW + time.toLocalDateTime().toLocalDate()));
                    loreList.add(Component.text(ChatColor.WHITE + "Time: " + ChatColor.GOLD + time.toLocalDateTime().toLocalTime() + " " + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)));
                    loreList.add(Component.text(""));
                    loreList.add(Component.text(ChatColor.WHITE + "UUID: " + ChatColor.GRAY + stringUuid));
                    loreList.add(Component.text(ChatColor.WHITE + "IP: " + ChatColor.GREEN + ip));
                    loreList.add(Component.text(""));
                    loreList.add(Component.text(ChatColor.LIGHT_PURPLE + "Click to print").clickEvent(ClickEvent.suggestCommand(String.valueOf(id))));

                    ItemMeta targetHeadMeta = targetHead.getItemMeta();
                    targetHeadMeta.displayName(Component.text(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + action));
                    targetHeadMeta.lore(loreList);
                    targetHead.setItemMeta(targetHeadMeta);

                    setMaterial(targetHead);

                    records.add(targetHead.clone());
                }
                callback.run();
            });
        });
    }

    /**
     * Sets the Material to the ItemStack by checking if the
     * display name of the ItemStack contains values of the
     * actionsMap HashMap and setting the value from the matching key.
     * If none from the actionsMap match, set as BEDROCK
     *
     * @param recordItem ItemStack from the records list
     */
    private void setMaterial(@NotNull ItemStack recordItem) {
        String itemName = ColorUtils.stripColor(recordItem.displayName());
        for (PlayerActivityType activityType : PlayerActivityType.values()) {
            if (itemName.contains(activityType.action)) {
                recordItem.setType(activityType.icon);
                return;
            }
        }

        // if none actions of the list match, set as BEDROCK
        recordItem.setType(Material.BEDROCK);
    }

    /**
     * Creates Inventories separated in pages.
     * Inventories are filled with record's ItemStacks
     * and Arrows to move between pages are set in each
     * Inventory. Book which shows the page number is also
     * set. All the inventories with all their contents
     * are then put in inventoryList
     *
     * @param player     The command sender
     * @param targetName The target's name
     */
    private void createPages(@NotNull Player player, @NotNull String targetName) {

        ItemStack nextPage = ItemManager.createItemStack(Material.ARROW, 1);
        ItemStack prevPage = ItemManager.createItemStack(Material.ARROW, 0);
        ItemStack pageInfo = ItemManager.createItemStack(Material.KNOWLEDGE_BOOK, 1);

        // how many pages are total
        int pagesCount = (int) Math.ceil(records.size() / 45d);

        /*
         loop through all pages and for each one and create a new inventory for each one.
         Add a book with display name which shows the current
         and the max page.
        */
        for (int i = 1; i <= Math.ceil(records.size() / 45d); i++) {
            Inventory inv = InventoryManager.createInventory(player, targetName + "'s activity", 6);
            pageInfo.setItemMeta(ItemManager.createItemMeta(pageInfo, ColorUtils.color("&2Page (" + i + "/" + pagesCount + ")"), new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES}));
            inv.setItem(49, pageInfo);
            inventoryList.add(inv);
        }

        int invCount = 0;
        int slot = 0;
        int max = 44;

        // gets the inventory 0 (first page)
        Inventory inv = inventoryList.get(invCount);

        /*
         loop through all the records and for each one, add it to the inventory and add +1 to slot
         if slot > max, the next page and previous page arrow is added to the inventory
         (if there is not 64 arrows already) and the inventory (page) is switched to the next one.
         The previous arrow will only be set if there is more than 1 inventory. Same for the next page arrow.
         the max int will be increased by 45 (To have 45 slots free for the next page to fill up)
        */
        for (ItemStack item : records) {

            // switch to the next inventory and add the nextPage arrow
            if (slot > max) {
                invCount++;
                nextPage.setItemMeta(ItemManager.createItemMeta(nextPage, nextPageName, null));

                // check to not go over the item limit
                if (nextPage.getAmount() < 64) {
                    nextPage.setAmount(nextPage.getAmount() + 1);
                }
                inv.setItem(50, nextPage);

                // if the inventory is already a second one, add the previousPage arrow
                if (invCount > 1) {
                    prevPage.setItemMeta(ItemManager.createItemMeta(prevPage, prevPageName, null));
                    if (prevPage.getAmount() < 64) {
                        prevPage.setAmount(prevPage.getAmount() + 1);
                    }
                    inv.setItem(48, prevPage);
                }

                // switch the inventory to the next one
                inv = inventoryList.get(invCount);

                // double the max amount to free up space for the next page
                max = max + 45;
            }
            inv.addItem(item.clone());
            slot++;
        }

        /*
        Set these values after the while loop ended. This is done, because otherwise
        on the last page, there wouldn't be previous page arrow. After the arrow is added
        to the inventory, the nextPage and previousPage arrows are set to the default values
        1 - as first page
        0 - as first page shouldn't have any previous page arrow
         */

        // check to not go over the item limit
        if (prevPage.getAmount() < 64 && prevPage.getAmount() != 0) {
            prevPage.setAmount(prevPage.getAmount() + 1);
        }
        prevPage.setItemMeta(ItemManager.createItemMeta(prevPage, prevPageName, null));
        inv.setItem(48, prevPage);

        nextPage.setAmount(1);
        prevPage.setAmount(0);
    }

    /**
     * When player clicks on an item in the inventory, check if the title contains "'s activity".
     * If so, check if list of different actions and their Material contains the item material.
     * or is BEDROCK (unknown action Material).
     * <p>
     * If true, perform command /activity-id ID as player to print all the data in chat, where
     * player can click on each info, and it will be copied to his clipboard. Also close the inventory.
     * <p>
     * If false, but the Material is ARROW, switch the page to previous or next
     * (will be decided in switchPage method)
     *
     * @param event When player clicks in the inventory.
     *              This is sometimes also called when opening and closing the inventory
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = ColorUtils.stripColor(event.getView().title());
        ItemStack item = event.getCurrentItem();

        /*
         if item or inventory is null, the event was probably executed
         when opening or closing the inventory, and not on click so return here.
        */
        if (item == null) return;
        if (inventory == null) return;

        if (title.contains("'s activity")) {
            try {
                if (PlayerActivityType.ALL_MATERIALS.contains(item.getType()) || item.getType() == Material.BEDROCK) {
                    /*
                     get the id from the click event of the item's lore.
                     NOTE: read more in createRecords comments
                    */
                    String id = Objects.requireNonNull(Objects.requireNonNull(
                                    item.lore()).get(6).clickEvent(), "Click event on item is null!")
                            .value();


                    Bukkit.dispatchCommand(humanEntity, "activity-id " + id);

                    inventory.close();
                } else if (item.getType() == Material.ARROW) {
                    switchPage(item, humanEntity);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Switch the page to the next one or the previous one.
     * That is decided by getting if the item display name
     * contains "Next page" or "Previous page". The pageCount
     * is always updated either by -1 or +1 depending on if the
     * page was switched to next or previous.
     *
     * @param item        ItemStack of the record
     * @param humanEntity Command sender
     */

    private void switchPage(@NotNull ItemStack item, @NotNull HumanEntity humanEntity) {
        String itemName = ColorUtils.stripColor(item.displayName());
        String nextPageNameString = ColorUtils.stripColor(nextPageName);
        String prevPageNameString = ColorUtils.stripColor(prevPageName);

        if (itemName.contains(nextPageNameString)) {
            pageCount++;
            Inventory nextInv = inventoryList.get(pageCount);
            humanEntity.openInventory(nextInv);
        } else if (itemName.contains(prevPageNameString)) {
            pageCount--;
            Inventory previousInv = inventoryList.get(pageCount);
            humanEntity.openInventory(previousInv);
        }
    }
}
