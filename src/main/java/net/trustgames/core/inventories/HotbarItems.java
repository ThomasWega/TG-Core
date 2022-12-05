package net.trustgames.core.inventories;

import net.trustgames.core.Core;
import net.trustgames.core.managers.HotbarManager;
import net.trustgames.core.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HotbarItems {

    private final Core core;

    public HotbarItems(Core core) {
        this.core = core;
    }

    public void addFromItemList(Player player){

        // get the functions
        ItemManager itemManager = new ItemManager();
        HotbarManager hotbarManager = new HotbarManager(core);

        // server selector
        ItemStack selectorStack = itemManager.createItemStack(Material.MAGMA_CREAM, 1);
        ItemMeta selectorMeta = hotbarManager.createItemMeta(selectorStack, ChatColor.GOLD + "Server Selector");
        selectorStack.setItemMeta(selectorMeta);

        // add the items to the inventory
        hotbarManager.addItemToInventory(player, 0, selectorStack);
    }

    public void updateHideItem(int count){

        HotbarManager hotbarManager = new HotbarManager(core);

        // hide players
        ItemStack hideStack = getHideStack();
        ItemMeta hideMeta = getHideMeta();
        hideStack.setItemMeta(hideMeta);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if(hideStack.getAmount() <= 64){
                hideStack.setAmount(count);
                hotbarManager.addItemToInventory(onlinePlayer, 7, hideStack);
            }
        }
    }

    // hide players
    public ItemStack getHideStack(){

        // get the functions
        ItemManager itemManager = new ItemManager();

        return itemManager.createItemStack(Material.FEATHER, 1);
    }

    public ItemMeta getHideMeta(){
        ItemStack hideStack = getHideStack();
        return hideStack.getItemMeta();
    }
}
