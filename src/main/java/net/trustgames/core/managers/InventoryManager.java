package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {

    // creates the inventory
    public static Inventory getInventory(Player player, int columns, String title) {
        int slots = columns * 9;
        if (columns > 6)
            Bukkit.getLogger().info(DebugColors.RED + "Inventory of player " + player.getName() + " has " + columns + " columns! The maximum is 6 (54 slots)");
        return Bukkit.createInventory(player, slots, Component.text(title));
    }

    // add multiple items to the inventory with ItemStack array
    public static void addItemsToInventory(Inventory inventory, ItemStack[] itemStacks) {
        inventory.setContents(itemStacks);
    }

    // add only one item to the inventory
    public static void addItemToInventory(Inventory inventory, int index, ItemStack itemStack) {
        inventory.setItem(index, itemStack);
    }

    // open the player inventory
    public static void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }
}
