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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender instanceof Player player) {
            if (player.hasPermission("core.staff")) {
                if (args[0].isEmpty()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes( '&', config.getString("messages.command-no-argument") + "&8 Use/activity <Player>"));
                    return true;
                }
                String target = args[0];
                ItemStack targetHead = ItemManager.createItemStack(Material.PLAYER_HEAD, 1);
                Inventory inventory = InventoryManager.getInventory(player, 6, target + "'s activity");

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

                        inventory.addItem(targetHead);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.openInventory(inventory);
            }
            else{
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

        if (title.equals(humanEntity.getName() + "'s activity")){
            ItemStack itemStack = event.getCurrentItem();

            String id = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(itemStack).lore()).get(5).clickEvent()).value();
            humanEntity.sendMessage(Component.text(ChatColor.YELLOW + "Click here to copy ID").clickEvent(ClickEvent.copyToClipboard(id)));

            Objects.requireNonNull(inventory).close();
        }
    }
}
