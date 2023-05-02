package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
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

    private final PlayerActivityFetcher activityFetcher;

    public PlayerActivityHandler(Core core) {
        this.activityFetcher = new PlayerActivityFetcher(core.getToolkit().getHikariManager());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        insertNewAction(event.getPlayer(),
                "JOIN " + Bukkit.getServer().getName() +
                        " (" + Bukkit.getServer().getPort() + ")");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        insertNewAction(event.getPlayer(),
                "LEAVE " + Bukkit.getServer().getName() +
                        " (" + Bukkit.getServer().getPort() + ")");
    }

    private void insertNewAction(Player player, String action) {
        InetSocketAddress playerIp = player.getAddress();
        String playerIpString = (playerIp == null) ? null : playerIp.getHostString();
        activityFetcher.insertNew(new PlayerActivity.Activity(
                player.getUniqueId(),
                playerIpString,
                action,
                new Timestamp(System.currentTimeMillis())
        ));
    }
}
