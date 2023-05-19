package net.trustgames.core.protection;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerDisabler implements Listener {


    public HungerDisabler(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }
}
