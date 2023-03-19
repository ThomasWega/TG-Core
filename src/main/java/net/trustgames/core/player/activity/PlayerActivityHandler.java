package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;
import java.sql.Timestamp;

/**
 * Creates a new player activity on PlayerJoin, PlayerQuit, etc.
 */
public final class PlayerActivityHandler implements Listener {

    private final Core core;
    private final PlayerActivityFetcher activityFetcher;

    public PlayerActivityHandler(Core core) {
        this.core = core;
        this.activityFetcher = new PlayerActivityFetcher(core);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress playerIp = player.getAddress();
        String playerIpString = (playerIp == null) ? null : playerIp.getHostString();
        UUIDCache uuidCache = new UUIDCache(core, player.getName());
        uuidCache.get(uuid -> {
            if (uuid == null) return;
            activityFetcher.insertNew(new PlayerActivity.Activity(
                    uuid,
                    playerIpString,
                    "JOIN " + Bukkit.getServer().getName() +
                            " (" + Bukkit.getServer().getPort() + ")",
                    new Timestamp(System.currentTimeMillis())
            ));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress playerIp = player.getAddress();
        String playerIpString = (playerIp == null) ? "null" : playerIp.getHostString();
        UUIDCache uuidCache = new UUIDCache(core, player.getName());
        uuidCache.get(uuid -> {
            if (uuid == null) return;
            activityFetcher.insertNew(new PlayerActivity.Activity(
                    uuid,
                    playerIpString,
                    "LEAVE " + Bukkit.getServer().getName() +
                            " (" + Bukkit.getServer().getPort() + ")",
                    new Timestamp(System.currentTimeMillis())
            ));
        });
    }
}
