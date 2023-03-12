package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This class is used for listeners for table player_activity
 */
public final class PlayerActivityHandler implements Listener {

    private final PlayerActivityFetcher activityFetcher;
    private final UUIDCache uuidCache;

    public PlayerActivityHandler(Core core) {
        this.activityFetcher = new PlayerActivityFetcher(core);
        this.uuidCache = core.getUuidCache();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        uuidCache.get(event.getPlayer().getName(), uuid -> activityFetcher.add(uuid, "JOIN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")"));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        uuidCache.get(event.getPlayer().getName(), uuid -> activityFetcher.add(uuid, "QUIT SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")"));
    }
}
