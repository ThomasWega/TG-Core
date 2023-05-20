package net.trustgames.core.managers.gui.player;

import net.trustgames.core.managers.gui.InventoryHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface PlayerInventoryHandler extends InventoryHandler {
    void onHotbarInteract(PlayerInteractEvent event);
    void onHotbarItemDrop(PlayerDropItemEvent event);
}
