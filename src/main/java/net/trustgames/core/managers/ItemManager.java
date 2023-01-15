package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;

/**
 * Handles the creation of the ItemStack,
 * their flags and ItemMeta
 */
public class ItemManager {

    /**
     * @param material ItemStack Material
     * @param count What amount
     * @return created ItemStack
     */
    public static ItemStack createItemStack(Material material, int count) {
        return new ItemStack(material, count);
    }

    /**
     * @param itemStack ItemStack to create ItemMeta to
     * @param name Display name of the ItemStack
     * @param itemFlags What items flags to put on the ItemStack. Can be null
     * @return created ItemMeta of the given ItemStack
     */
    public static ItemMeta createItemMeta(ItemStack itemStack, String name, @Nullable ItemFlag[] itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));

        if (!(itemFlags == null)){
            itemMeta.addItemFlags(itemFlags);
        }

        itemStack.setItemMeta(itemMeta);
        return itemMeta;
    }
}
