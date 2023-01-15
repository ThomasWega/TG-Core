package net.trustgames.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Handles the registration of events
 */
public class EventManager {

    /**
     * @param listener Listener class of the event
     * @param plugin Plugin instance
     */
    public static void registerEvent(Listener listener, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
