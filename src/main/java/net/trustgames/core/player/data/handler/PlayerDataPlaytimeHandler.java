package net.trustgames.core.player.data.handler;

import net.trustgames.core.Core;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.database.player.data.PlayerDataFetcher;
import net.trustgames.toolkit.database.player.data.config.PlayerDataType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataPlaytimeHandler implements Listener {

    private final Toolkit toolkit;
    private final Map<UUID, Long> startTimes = new HashMap<>();

    public PlayerDataPlaytimeHandler(Core core) {
        this.toolkit = core.getToolkit();
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        long startTime = System.currentTimeMillis();
        startTimes.put(playerId, startTime);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        long endTime = System.currentTimeMillis();
        long durationInMillis = endTime - startTimes.get(uuid);
        int durationInSec = (int) Math.floor(durationInMillis / 1000d);
        new PlayerDataFetcher(toolkit).addDataAsync(uuid, PlayerDataType.PLAYTIME, durationInSec);
    }
}

