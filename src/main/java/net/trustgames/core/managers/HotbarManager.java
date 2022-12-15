package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HotbarManager implements Listener {

    // add multiple items to the inventory with ItemStack array
    public void addItemsToInventory(Player player, ItemStack[] itemStacks) {
        Inventory inventory = player.getInventory();
        inventory.setContents(itemStacks);
    }

    // add only one item to the inventory
    public void addItemToInventory(Player player, int index, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        inventory.setItem(index, itemStack);
    }

    // creates the itemMeta
    public ItemMeta createItemMeta(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return itemMeta;
    }
}
