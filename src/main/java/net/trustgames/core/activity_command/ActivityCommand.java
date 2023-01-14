package net.trustgames.core.activity_command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trustgames.core.Core;
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

public class ActivityCommand implements CommandExecutor, Listener {

    private final Core core;

    public ActivityCommand(Core core) {
        this.core = core;
    }

    private static final List<ItemStack> records = new ArrayList<>();
    private static final List<Inventory> inventoryList = new ArrayList<>();

    ItemStack nextPage = ItemManager.createItemStack(Material.ARROW, 1);
    ItemStack previousPage = ItemManager.createItemStack(Material.ARROW, 0);
    ItemStack pageInfo = ItemManager.createItemStack(Material.KNOWLEDGE_BOOK, 1);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender instanceof Player) {
            if (sender.hasPermission("core.staff")) {
                if (args.length != 1) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.command-invalid-argument") + "&8 Use /activity <player>"));
                    return true;
                }

                Player player = ((Player) sender).getPlayer();

                if (player == null) return true;

                records.clear();
                inventoryList.clear();
                nextPage.setAmount(1);
                previousPage.setAmount(0);

                String target = args[0];
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                createRecords(offlinePlayer, target);

                if (records.isEmpty()){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(Objects.requireNonNull(config.getString("messages.command-no-player-activity")), target)));
                    return true;
                }

                createPages(player);
                player.openInventory(inventoryList.get(0));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("messages.no-permission"))));
            }
        } else {
            Bukkit.getLogger().info(Objects.requireNonNull(core.getConfig().getString("messages.only-in-game-command")));
        }
        return true;
    }

    public void createPages(Player player){
        int pagesCount = (int) Math.ceil(records.size() / 45d);

        for (int i = 1; i <= Math.ceil(records.size() / 45d); i++) {
            Inventory inv = InventoryManager.getInventory(player, 6, player.getName()  + "'s activity");
            pageInfo.setItemMeta(ItemManager.createItemMeta(pageInfo, ChatColor.DARK_GREEN + "Page (" + i + "/" + pagesCount + ")", new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES}));
            inv.setItem(49, pageInfo);
            inventoryList.add(inv);

        }

        int invCount = 0;
        int slot = 0;
        int max = 44;

        Inventory inv = inventoryList.get(invCount);
        for (ItemStack item : records){
            if (slot > max){
                invCount++;
                nextPage.setItemMeta(ItemManager.createItemMeta(nextPage, ChatColor.YELLOW + "Next page", null));
                if (nextPage.getAmount() < 64){
                    nextPage.setAmount(nextPage.getAmount() + 1);
                }
                inv.setItem(50, nextPage);

                if (invCount > 1){
                    previousPage.setItemMeta(ItemManager.createItemMeta(previousPage, ChatColor.YELLOW + "Previous page", null));
                    if (previousPage.getAmount() < 64) {
                        previousPage.setAmount(previousPage.getAmount() + 1);
                    }
                    inv.setItem(48, previousPage);
                }

                inv = inventoryList.get(invCount);
                max = max + 45;
            }
            inv.addItem(item.clone());
            slot++;
        }

        if (previousPage.getAmount() < 64){
            previousPage.setAmount(previousPage.getAmount() + 1);
        }
        inv.setItem(48, previousPage);

        nextPage.setAmount(1);
        previousPage.setAmount(0);

        nextPage(player);
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        String itemName = PlainTextComponentSerializer.plainText().serialize(item.displayName());

        if (inventory == null) return;

        if (title.contains(humanEntity.getName() + "'s activity")) {
            try {
                if (actionsList.containsValue(item.getType())) {
                    String id = Objects.requireNonNull(Objects.requireNonNull(item.lore()).get(5).clickEvent()).value();

                    Player player = Bukkit.getPlayer(humanEntity.getUniqueId());
                    if (player == null) return;
                    if (!player.performCommand("activity-id " + id)){
                        humanEntity.sendMessage(ChatColor.RED + "ERROR when executing /activity-id " + id);
                    }
                    inventory.close();
                }

                else if (item.getType() == Material.ARROW) {
                    if (itemName.contains("Next page")) {
                        nextPage(humanEntity);
                    } else if (itemName.contains("Previous page")) {
                        previousPage(humanEntity);
                    }
                }
            }
            catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }
    }

    public void nextPage(HumanEntity humanEntity){

        int nextPageCount = nextPage.getAmount() + 1;
        int previousPageCount = previousPage.getAmount() + 1;

        nextPage.setAmount(nextPageCount);
        previousPage.setAmount(previousPageCount);

        Inventory nextInv = inventoryList.get(nextPage.getAmount());

        humanEntity.openInventory(nextInv);
    }

    public void previousPage(HumanEntity humanEntity){
        int nextPageCount = nextPage.getAmount() - 1;
        int previousPageCount = previousPage.getAmount() - 1;

        Inventory previousInv = inventoryList.get(previousPage.getAmount());

        nextPage.setAmount(nextPageCount);
        previousPage.setAmount(previousPageCount);

        humanEntity.openInventory(previousInv);
    }

    public static final HashMap<String, Material> actionsList = new HashMap<>();

    public void setItemType(ItemStack recordItem){
        String itemName = PlainTextComponentSerializer.plainText().serialize(recordItem.displayName());

        actionsList.put("JOIN SERVER", Material.GREEN_BED);
        actionsList.put("QUIT SERVER", Material.RED_BED);
        actionsList.put("QUIT SHUTDOWN SERVER", Material.BLACK_BED);

        for (String action : actionsList.keySet()) {
            if (itemName.contains(action)) {
                recordItem.setType(actionsList.get(action));
                return;
            }
        }

        recordItem.setType(Material.BEDROCK);
    }

    public void createRecords(OfflinePlayer offlinePlayer, String targetName){
        ActivityQuery activityQuery = new ActivityQuery(core);
        ResultSet resultSet = activityQuery.getActivityByUUID(offlinePlayer.getUniqueId().toString());
        ItemStack targetHead = ItemManager.createItemStack(Material.PAINTING, 1);


        try {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                //     String uuid = resultSet.getString("uuid");
                String ip = resultSet.getString("ip");
                String action = resultSet.getString("action");
                Timestamp time = resultSet.getTimestamp("time");
                String encodedId = activityQuery.encodeId(id);

                List<Component> loreList = new ArrayList<>();
                loreList.add(Component.text(ChatColor.WHITE + "Date: " + ChatColor.YELLOW + time.toLocalDateTime().toLocalDate()));
                loreList.add(Component.text(ChatColor.WHITE + "Time: " + ChatColor.GOLD + time.toLocalDateTime().toLocalTime() + " " + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)));
                loreList.add(Component.text(""));
                loreList.add(Component.text(ChatColor.WHITE + "IP: " + ChatColor.GREEN + ip));
                loreList.add(Component.text(""));
                loreList.add(Component.text(ChatColor.GRAY + "Click to print").clickEvent(ClickEvent.suggestCommand(encodedId)));

                ItemMeta targetHeadMeta = targetHead.getItemMeta();
                targetHeadMeta.displayName(Component.text(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + action));
                targetHeadMeta.lore(loreList);

                targetHead.setItemMeta(targetHeadMeta);

                setItemType(targetHead);

                records.add(targetHead.clone());

                Inventory inventory = InventoryManager.getInventory(offlinePlayer.getPlayer(), 6, targetName + "'s activity");
                inventory.addItem(targetHead);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

/*
TODO/FIXME
- when opened second time, it goes to the page it was last closed at
 */
