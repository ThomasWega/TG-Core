package net.trustgames.core.inventories;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Hotbar implements Listener {

    private final Core core;

    public Hotbar(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // get the other methods
        HotbarItems hotbarItems = new HotbarItems(core);
        hotbarItems.addFromItemList(player);

        // hide players
        hotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // get methods
        HotbarItems hotbarItems = new HotbarItems(core);

        // hide players
        hotbarItems.updateHideItem(Bukkit.getOnlinePlayers().size() - 1);
    }
}
