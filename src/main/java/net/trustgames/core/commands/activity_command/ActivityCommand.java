package net.trustgames.core.commands.activity_command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trustgames.core.Core;
import net.trustgames.core.managers.ColorManager;
import net.trustgames.core.managers.InventoryManager;
import net.trustgames.core.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Opens up a menu with all the given target's logged activity.
 * The activity is separated in to pages and different actions
 * are differentiated by different Materials. The player can click
 * the item and all the data will be printed in chat.
 */
public class ActivityCommand implements CommandExecutor, Listener {

    private final Core core;

    public ActivityCommand(Core core) {
        this.core = core;
    }

    /** Stores all ItemStack with the data for each row */
    private static final List<ItemStack> records = new ArrayList<>();

    /** Stores all Inventories */
    private static final List<Inventory> inventoryList = new ArrayList<>();

    /** Stores all the Actions and their corresponding ItemStack */
    private static final HashMap<String, Material> actionsMap = new HashMap<>();

    /**
     * Used for switching pages.
     * +1 everytime page is switched to next page.
     * -1 everytime page is switched to previous page.
     * */
    private static int pageCount = 0;


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender instanceof Player) {
            if (sender.hasPermission("core.staff")) {

                if (core.getMariaDB().isMySQLDisabled()){
                    String path = "messages.mariadb.disabled";
                    sender.sendMessage(ColorManager.color(Objects.requireNonNull(
                            config.getString(path), "String on path " + path + " wasn't found in config!")));
                    return true;
                }

                if (args.length != 1) {
                    sender.sendMessage(ColorManager.color(
                            config.getString("messages.command.invalid-argument") + "&8 Use /activity <Player/UUID>"));
                    return true;
                }

                Player player = ((Player) sender).getPlayer();
                if (player == null) return true;


                String target = args[0];

                /*
                 check if the supplied target is uuid or a player name by
                 trying to convert it to uuid. If it succeeds, its uuid and
                 the target is set the offlinePlayer's name. If it fails
                 and throws exception, it's the player's name and the target doesn't
                 need to be changed.
                */
                OfflinePlayer offlinePlayer;
                try {
                    UUID uuid = UUID.fromString(target);
                    offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    target = offlinePlayer.getName();
                }
                catch (IllegalArgumentException e){
                    offlinePlayer = Bukkit.getOfflinePlayer(target);
                }

                /*
                 if the menus were previously opened, all the maps
                 need to be cleared to avoid the items showing twice.
                 The int needs to be reset so when the menu is opened a
                 second time, the pages are reset to the first one
                */
                records.clear();
                inventoryList.clear();
                actionsMap.clear();
                pageCount = 0;

                createRecords(offlinePlayer);

                if (records.isEmpty()){
                    String path = "messages.command.no-player-activity";
                    sender.sendMessage(ColorManager.color(String.format(Objects.requireNonNull(
                            config.getString(path), "String on path " + path + " wasn't found in config!"), target)));
                    return true;
                }

                createPages(player, target);

                // open the first inventory (first page) from the list
                player.openInventory(inventoryList.get(0));
            } else {
                String path = "messages.no-permission";
                sender.sendMessage(ColorManager.color(Objects.requireNonNull(
                        config.getString(path), "String on path " + path + " wasn't found in config!")));
            }
        } else {
            String path = "messages.command.only-in-game";
            Bukkit.getLogger().warning(Objects.requireNonNull(
                    config.getString(path), "String on path " + path + " wasn't found in config!"));
        }
        return true;
    }

    /**
     * Gets the ResultSet of offlinePlayer (target) from ActivityQuery
     * and for each result, it creates and ItemStack with display name
     * and lore with the corresponding data
     *
     * @param offlinePlayer Target player
     */
    private void createRecords(OfflinePlayer offlinePlayer){
        ActivityQuery activityQuery = new ActivityQuery(core);

        ResultSet resultSet = activityQuery.getActivityByUUID(offlinePlayer.getUniqueId().toString());
        ItemStack targetHead = ItemManager.createItemStack(Material.PAINTING, 1);

        /*
        loop through all the results and for each one, set the corresponding
        id, uuid, ip, and time to the lore and set action as the display name.
        Also set the correct Material by using setMaterial method.
        Then add a clone of the ItemStack to the records list
         */
        try {
            while (resultSet.next()) {

                // results
                String id = resultSet.getString("id");
                String uuid = resultSet.getString("uuid");
                String ip = resultSet.getString("ip");
                String action = resultSet.getString("action");
                Timestamp time = resultSet.getTimestamp("time");
                String encodedId = activityQuery.encodeId(id);

                /*
                One of the lore lines needs to have a click event with the value of the encodedID. This is
                because in other methods it's required to retrieve the encodedId by just clicking on the item.
                The click event is technically never being executed, as the player doesn't click on the lore, but
                on the ItemStack, but the click event value is still present in the ItemStack's lore and can be retrieved.
                That's how I get the encodedId from the ItemStack later on.
                 */
                List<Component> loreList = new ArrayList<>();
                loreList.add(Component.text(ChatColor.WHITE + "Date: " + ChatColor.YELLOW + time.toLocalDateTime().toLocalDate()));
                loreList.add(Component.text(ChatColor.WHITE + "Time: " + ChatColor.GOLD + time.toLocalDateTime().toLocalTime() + " " + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)));
                loreList.add(Component.text(""));
                loreList.add(Component.text(ChatColor.WHITE + "UUID: " + ChatColor.GRAY + uuid));
                loreList.add(Component.text(ChatColor.WHITE + "IP: " + ChatColor.GREEN + ip));
                loreList.add(Component.text(""));
                loreList.add(Component.text(ChatColor.LIGHT_PURPLE + "Click to print").clickEvent(ClickEvent.suggestCommand(encodedId)));

                ItemMeta targetHeadMeta = targetHead.getItemMeta();
                targetHeadMeta.displayName(Component.text(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + action));
                targetHeadMeta.lore(loreList);
                targetHead.setItemMeta(targetHeadMeta);

                setMaterial(targetHead);

                records.add(targetHead.clone());
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("ERROR: Trying loop through ResultSet in ActivityCommand class");
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the Material to the ItemStack by checking if the
     * display name of the ItemStack contains values of the
     * actionsMap HashMap and setting the value from the matching key.
     * If none from the actionsMap match, set as BEDROCK
     *
     * @param recordItem ItemStack from the records list
     */
    private void setMaterial(ItemStack recordItem){
        String itemName = PlainTextComponentSerializer.plainText().serialize(recordItem.displayName());

        /*
         the list of possible actions names.
         If more actions are started logging, they need to be added here
        */
        actionsMap.put("JOIN SERVER", Material.GREEN_BED);
        actionsMap.put("QUIT SERVER", Material.RED_BED);
        actionsMap.put("QUIT SHUTDOWN SERVER", Material.BLACK_BED);

        for (String action : actionsMap.keySet()) {
            if (itemName.contains(action)) {
                recordItem.setType(actionsMap.get(action));
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
     * @param player The command sender
     * @param targetName The target's name
     */
    private void createPages(Player player, String targetName){

        ItemStack nextPage = ItemManager.createItemStack(Material.ARROW, 1);
        ItemStack previousPage = ItemManager.createItemStack(Material.ARROW, 0);
        ItemStack pageInfo = ItemManager.createItemStack(Material.KNOWLEDGE_BOOK, 1);

        // how many pages are total
        int pagesCount = (int) Math.ceil(records.size() / 45d);

        /*
         loop through all pages and for each one and create a new inventory for each one.
         Add a book with display name which shows the current
         and the max page.
        */
        for (int i = 1; i <= Math.ceil(records.size() / 45d); i++) {
            Inventory inv = InventoryManager.createInventory(player, 6, targetName  + "'s activity");
            pageInfo.setItemMeta(ItemManager.createItemMeta(pageInfo, ChatColor.DARK_GREEN + "Page (" + i + "/" + pagesCount + ")", new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES}));
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
        for (ItemStack item : records){

            // switch to the next inventory and add the nextPage arrow
            if (slot > max){
                invCount++;
                nextPage.setItemMeta(ItemManager.createItemMeta(nextPage, ChatColor.YELLOW + "Next page", null));

                // check to not go over the item limit
                if (nextPage.getAmount() < 64){
                    nextPage.setAmount(nextPage.getAmount() + 1);
                }
                inv.setItem(50, nextPage);

                // if the inventory is already a second one, add the previousPage arrow
                if (invCount > 1){
                    previousPage.setItemMeta(ItemManager.createItemMeta(previousPage, ChatColor.YELLOW + "Previous page", null));
                    if (previousPage.getAmount() < 64) {
                        previousPage.setAmount(previousPage.getAmount() + 1);
                    }
                    inv.setItem(48, previousPage);
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
        if (previousPage.getAmount() < 64){
            previousPage.setAmount(previousPage.getAmount() + 1);
        }
        previousPage.setItemMeta(ItemManager.createItemMeta(previousPage, ChatColor.YELLOW + "Previous page", null));
        inv.setItem(48, previousPage);

        nextPage.setAmount(1);
        previousPage.setAmount(0);
    }

    /**
     * When player clicks on an item in the inventory, check if the title contains "'s activity".
     * If so, check if list of different actions and their Material contains the item material.
     * or is BEDROCK (unknown action Material).
     *
     * If true, perform command /activity-id ID as player to print all the data in chat, where
     * player can click on each info, and it will be copied to his clipboard. Also close the inventory.
     *
     * If false, but the Material is ARROW, switch the page to previous or next
     * (will be decided in switchPage method)
     *
     * @param event When player clicks in the inventory.
     *              This is sometimes also called when opening and closing the inventory
     */
    @EventHandler
    private void onPlayerClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        ItemStack item = event.getCurrentItem();

        /*
         if item or inventory is null, the event was probably executed
         when opening or closing the inventory, and not on click so return here.
        */
        if (item == null) return;
        if (inventory == null) return;

        if (title.contains("'s activity")) {
            try {
                if (actionsMap.containsValue(item.getType()) || item.getType() == Material.BEDROCK) {

                    /*
                     get the id from the click event of the item's lore.
                     NOTE: read more in createRecords comments
                    */
                    String id = Objects.requireNonNull(Objects.requireNonNull(
                            item.lore()).get(6).clickEvent(), "Click event on item is null!")
                            .value();

                    Player player = Bukkit.getPlayer(humanEntity.getUniqueId());
                    if (player == null) return;

                    if (!player.performCommand("activity-id " + id)){
                        humanEntity.sendMessage(ChatColor.RED + "ERROR: Executing /activity-id " + id + " as a player");
                    }
                    inventory.close();
                }

                else if (item.getType() == Material.ARROW) {
                    switchPage(item, humanEntity);
                }
            }
            catch (IndexOutOfBoundsException e){
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
     * @param item ItemStack of the record
     * @param humanEntity Command sender
     */

    private void switchPage(ItemStack item, HumanEntity humanEntity){
        String itemName = PlainTextComponentSerializer.plainText().serialize(item.displayName());

        if (itemName.contains("Next page")) {
            pageCount++;
            Inventory nextInv = inventoryList.get(pageCount);
            humanEntity.openInventory(nextInv);
        } else if (itemName.contains("Previous page")) {
            pageCount--;
            Inventory previousInv = inventoryList.get(pageCount);
            humanEntity.openInventory(previousInv);
        }
    }
}
