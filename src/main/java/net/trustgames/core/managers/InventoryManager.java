package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {

    // creates the inventory
    public Inventory getInventory(Player player, int columns, String title) {
        return Bukkit.createInventory(player, columns * 9, Component.text(title));
    }

    // add multiple items to the inventory with ItemStack array
    public void addItemsToInventory(Inventory inventory, ItemStack[] itemStacks) {
        inventory.setContents(itemStacks);
    }

    // add only one item to the inventory
    public void addItemToInventory(Inventory inventory, int index, ItemStack itemStack) {
        inventory.setItem(index, itemStack);
    }

    // open the player inventory
    public void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }
}
