package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static net.trustgames.core.Core.LOGGER;

/**
 * Handles the creation of the inventory (gui)
 */
public abstract class InventoryManager {

    /**
     * @param player  Owner of the inventory
     * @param title   Title of the inventory
     * @param columns Number of columns (max 6)
     * @return created Inventory
     */
    public static Inventory createInventory(@NotNull Player player,
                                            @NotNull String title,
                                            int columns) {
        int slots = columns * 9;
        if (columns > 6)
            LOGGER.severe("Inventory of player " + player.getName() + " has " + columns + " columns! The maximum is 6 (54 slots)");
        return Bukkit.createInventory(player, slots, Component.text(title));
    }
}
