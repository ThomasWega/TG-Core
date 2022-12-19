package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {

    // create the itemStack
    public static ItemStack createItemStack(Material material, int count) {
        return new ItemStack(material, count);
    }

    // creates the itemMeta
    public static ItemMeta createItemMeta(ItemStack itemStack, String name, ItemFlag[] itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        itemMeta.addItemFlags(itemFlags);
        itemStack.setItemMeta(itemMeta);
        return itemMeta;
    }
}
