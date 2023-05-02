package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.cache.UUIDCache;
import net.trustgames.toolkit.database.player.activity.PlayerActivity;
import net.trustgames.toolkit.database.player.activity.PlayerActivityFetcher;
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

    private final Toolkit toolkit;
    private final PlayerActivityFetcher activityFetcher;

    public PlayerActivityHandler(Core core) {
        this.toolkit = core.getToolkit();
        this.activityFetcher = new PlayerActivityFetcher(toolkit.getHikariManager());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress playerIp = player.getAddress();
        String playerIpString = (playerIp == null) ? null : playerIp.getHostString();
        UUIDCache uuidCache = new UUIDCache(toolkit, player.getName());
        uuidCache.get(uuid -> {
            if (uuid.isEmpty()) return;
            activityFetcher.insertNew(new PlayerActivity.Activity(
                    uuid.get(),
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
        String playerIpString = (playerIp == null) ? null : playerIp.getHostString();
        UUIDCache uuidCache = new UUIDCache(toolkit, player.getName());
        uuidCache.get(uuid -> {
            if (uuid.isEmpty()) return;
            activityFetcher.insertNew(new PlayerActivity.Activity(
                    uuid.get(),
                    playerIpString,
                    "LEAVE " + Bukkit.getServer().getName() +
                            " (" + Bukkit.getServer().getPort() + ")",
                    new Timestamp(System.currentTimeMillis())
            ));
        });
    }
}
