package net.trustgames.core.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    // create the itemStack
    public ItemStack createItemStack(Material material, int count) {
        return new ItemStack(material, count);
    }
}