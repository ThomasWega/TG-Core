package net.trustgames.core.player.uuid;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerUUIDHandler implements Listener {

    private final Core core;

    private final PlayerUUIDFetcher uuidFetcher;

    public PlayerUUIDHandler(Core core) {
        this.core = core;
        this.uuidFetcher = new PlayerUUIDFetcher(core);
    }


    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID uuid = UUIDCache.get(playerName);
        uuidFetcher.write(uuid, playerName);
    }
}
