package net.trustgames.core.inventories;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.managers.HotbarManager;
import net.trustgames.core.managers.ItemManager;
import net.trustgames.core.managers.SkullManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class HotbarItems {

    private final Core core;

    public HotbarItems(Core core) {
        this.core = core;
    }

    // list of hotbar items to add
    public void addFromItemList(Player player) {

        // get the functions
        ItemManager itemManager = new ItemManager();
        HotbarManager hotbarManager = new HotbarManager(core);
        SkullManager skullManager = new SkullManager();

        // server selector
        ItemStack selectorStack = skullManager.getSkull("http://textures.minecraft.net/texture/7a4b8832afc3cea83224b14410b662e707e8e79c61f466e362a30e82c7de9");
        ItemMeta selectorMeta = hotbarManager.createItemMeta(selectorStack, ChatColor.GOLD + "Server Selector");
        selectorStack.setItemMeta(selectorMeta);

        // player profile
        ItemStack profileStack = itemManager.createItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta profileMeta = hotbarManager.createItemMeta(profileStack, ChatColor.GREEN + "My Profile");
        SkullMeta skullProfileMeta = (SkullMeta) profileMeta;
        skullProfileMeta.setOwningPlayer(player);

        // add the items to the inventory
        hotbarManager.addItemToInventory(player, 0, selectorStack);
        hotbarManager.addItemToInventory(player, 5, selectorStack);

    }

    // update the hide item in hotbar
    public void updateHideItem(int count) {

        // get the methods
        ItemManager itemManager = new ItemManager();
        HotbarManager hotbarManager = new HotbarManager(core);

        // hide players item
        ItemStack hideStack = itemManager.createItemStack(Material.FEATHER, 1);
        ItemMeta hideMeta = hideStack.getItemMeta();
        hideMeta.displayName(Component.text(ChatColor.WHITE + "Hide Players" + ChatColor.GRAY + " (Use)"));

        // lore
        List<Component> hideLore = new ArrayList<>();
        hideLore.add(Component.text(""));
        hideLore.add(Component.text(ChatColor.DARK_GRAY + "Hide " + (Bukkit.getOnlinePlayers().size() - 1) + " players by"));
        hideLore.add(Component.text(ChatColor.DARK_GRAY + "clicking with this item"));
        hideMeta.lore(hideLore);

        // set the item meta
        hideStack.setItemMeta(hideMeta);

        // loop through the online player and set for each one the new amount
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (hideStack.getAmount() <= 64) {
                hideStack.setAmount(count);
                hotbarManager.addItemToInventory(onlinePlayer, 7, hideStack);
            }
        }
    }

    // update the player profile item in hotbar
    public void updateProfileItem(int count) {
        // TODO finish
    }
}
