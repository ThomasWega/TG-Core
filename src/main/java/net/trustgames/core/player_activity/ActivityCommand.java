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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ActivityCommand implements CommandExecutor, Listener {

    private final Core core;

    public ActivityCommand(Core core) {
        this.core = core;
    }

    private static final List<ItemStack> records = new ArrayList<>();
    private static final List<Inventory> inventoryList = new ArrayList<>();

    ItemStack nextPage = ItemManager.createItemStack(Material.ARROW, 1);
    ItemStack previousPage = ItemManager.createItemStack(Material.ARROW, 1);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender instanceof Player player) {
            if (player.hasPermission("core.staff")) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.command-no-argument") + "&8 Use /activity <player>"));
                    return true;
                }

                records.clear();
                inventoryList.clear();

                String target = args[0];
                ItemStack targetHead = ItemManager.createItemStack(Material.PAINTING, 1);

                ActivityQuery activityQuery = new ActivityQuery(core);
                ResultSet resultSet = activityQuery.getActivity(player.getUniqueId().toString());
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

                        records.add(targetHead.clone());

                        Inventory inventory = InventoryManager.getInventory(player, 6, target + "'s activity" + " (1/" + ((int) Math.ceil(records.size() / 44d)) + ")");
                        inventory.addItem(targetHead);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                createPages(player);
                player.openInventory(inventoryList.get(0));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("messages.no-permission"))));
            }
        } else {
            Bukkit.getLogger().info(Objects.requireNonNull(core.getConfig().getString("messages.only-in-game-command")));
        }
        return true;
    }


    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains(humanEntity.getName() + "'s activity")) {
            ItemStack itemStack = event.getCurrentItem();

            try {
                if (itemStack != null && itemStack.lore() != null && PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(itemStack.lore()).get(5)).contains("ID")) {
                    String id = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(itemStack).lore()).get(5).clickEvent()).value();
                    humanEntity.sendMessage(Component.text(ChatColor.YELLOW + "Click here to copy ID").clickEvent(ClickEvent.copyToClipboard(id)));

                    Objects.requireNonNull(inventory).close();
                }
            }
            catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }
    }

    public void createPages(Player player){

        for (int i = 1; i <= Math.ceil(records.size() / 44d); i++) {
            inventoryList.add(InventoryManager.getInventory(player, 6, player.getName()  + "'s activity" + " (" + i + "/" + ((int) Math.ceil(records.size() / 44d)) + ")"));
        }

        int invCount = 0;
        int slot = 0;
        int max = 44;

        Inventory inv = inventoryList.get(invCount);
        for (ItemStack item : records){
            if (slot > max){
                invCount++;
                nextPage.setAmount(invCount);
                nextPage.setItemMeta(ItemManager.createItemMeta(nextPage, ChatColor.YELLOW + "Next page", null));
                inv.setItem(50, nextPage);
                inv = inventoryList.get(invCount);
                max = max + 44;
            }
            inv.addItem(item.clone());
            slot++;
        }

        nextPage(player);
    }

    @EventHandler
    public void onArrowClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        String itemName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(item).displayName());
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains(player.getName() + "'s activity")) {
            if (item.getType() == Material.ARROW) {
                if (itemName.contains("Next page")) {
                    System.out.println("NEXT PAGE EVENT");
                    nextPage(player);
                } else if (itemName.contains("Previous page")) {
                    // TODO previousPage(player);
                }
            }
        }
    }

    public void nextPage(Player player){
        int nextPageCount = nextPage.getAmount();

        player.closeInventory();
        System.out.println("CLOSED INV");

        player.openInventory(inventoryList.get(nextPageCount));
    }
}
