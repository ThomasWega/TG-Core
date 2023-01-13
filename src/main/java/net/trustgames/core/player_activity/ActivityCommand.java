package net.trustgames.core.player_activity;

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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender instanceof Player) {
            if (sender.hasPermission("core.staff")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.command-no-argument") + "&8 Use /activity <player>"));
                    return true;
                }

                records.clear();
                inventoryList.clear();
                nextPage.setAmount(1);
                previousPage.setAmount(0);

                String target = args[0];
                OfflinePlayer offlinePlayer = core.getServer().getOfflinePlayer(target); // TODO take from database

                if (offlinePlayer == null){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(Objects.requireNonNull(config.getString("messages.command-invalid-player")), target)));
                    return true;
                }

                ItemStack targetHead = ItemManager.createItemStack(Material.PAINTING, 1);

                ActivityQuery activityQuery = new ActivityQuery(core);
                ResultSet resultSet = activityQuery.getActivity(offlinePlayer.getUniqueId().toString());
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
                        loreList.add(Component.text(ChatColor.GRAY + "Click to copy ID").clickEvent(ClickEvent.suggestCommand(encodedId)));

                        ItemMeta targetHeadMeta = targetHead.getItemMeta();
                        targetHeadMeta.displayName(Component.text(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + action));
                        targetHeadMeta.lore(loreList);

                        targetHead.setItemMeta(targetHeadMeta);

                        setItemType(targetHead);

                        records.add(targetHead.clone());

                        Inventory inventory = InventoryManager.getInventory(offlinePlayer.getPlayer(), 6, target + "'s activity" + " (1/" + ((int) Math.ceil(records.size() / 44d)) + ")");
                        inventory.addItem(targetHead);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                createPages(offlinePlayer.getPlayer());
                offlinePlayer.getPlayer().openInventory(inventoryList.get(0));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("messages.no-permission"))));
            }
        } else {
            Bukkit.getLogger().info(Objects.requireNonNull(core.getConfig().getString("messages.only-in-game-command")));
        }
        return true;
    }

    public void createPages(Player player){

        for (int i = 1; i <= Math.ceil(records.size() / 45d); i++) {
            inventoryList.add(InventoryManager.getInventory(player, 6, player.getName()  + "'s activity" + " (" + i + "/" + ((int) Math.ceil(records.size() / 45d)) + ")"));
        }

        int invCount = 0;
        int slot = 0;
        int max = 44;

        Inventory inv = inventoryList.get(invCount);
        for (ItemStack item : records){
            if (slot > max){
                invCount++;
                nextPage.setItemMeta(ItemManager.createItemMeta(nextPage, ChatColor.YELLOW + "Next page", null));
                nextPage.setAmount(nextPage.getAmount() + 1);
                inv.setItem(50, nextPage);

                if (invCount > 1){
                    previousPage.setItemMeta(ItemManager.createItemMeta(previousPage, ChatColor.YELLOW + "Previous page", null));
                    previousPage.setAmount(previousPage.getAmount() + 1);
                    inv.setItem(48, previousPage);
                }

                inv = inventoryList.get(invCount);
                max = max + 45;
            }
            inv.addItem(item.clone());
            slot++;
        }

        previousPage.setAmount(previousPage.getAmount() + 1);
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
        String itemName = PlainTextComponentSerializer.plainText().serialize(item.displayName());

        if (inventory == null) return;

        if (title.contains(humanEntity.getName() + "'s activity")) {
            try {
                if (item.getType() == Material.PAINTING) {
                    String id = item.lore().get(5).clickEvent().value();
                    humanEntity.sendMessage(Component.text(ChatColor.YELLOW + "Click here to copy ID").clickEvent(ClickEvent.copyToClipboard(id)));

                    inventory.close();
                }

                else if (item.getType() == Material.ARROW) {
                    if (itemName.contains("Next page")) {
                        System.out.println("NEXT PAGE EVENT");
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

        System.out.println("OPENED NEXT INV");
        System.out.println(nextPage.getAmount() + " | " + previousPage.getAmount());

        Inventory nextInv = inventoryList.get(nextPage.getAmount());

        humanEntity.openInventory(nextInv);
    }

    public void previousPage(HumanEntity humanEntity){
        int nextPageCount = nextPage.getAmount() - 1;
        int previousPageCount = previousPage.getAmount() - 1;


        System.out.println("OPENED PREVIOUS INV");
        System.out.println(nextPage.getAmount() + " | " + previousPage.getAmount());

        Inventory previousInv = inventoryList.get(previousPage.getAmount());

        nextPage.setAmount(nextPageCount);
        previousPage.setAmount(previousPageCount);

        humanEntity.openInventory(previousInv);
    }

    public void setItemType(ItemStack recordItem){
        String itemName = PlainTextComponentSerializer.plainText().serialize(recordItem.displayName());

        HashMap<String, Material> actionsList = new HashMap<>();
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
}

/*
TODO/FIXME
- when opened second time, it goes to the page it was last closed at
- create a new database with only player uuid (to know if he ever joined before)
    - make OfflinePlayer get the player from the database
 */
