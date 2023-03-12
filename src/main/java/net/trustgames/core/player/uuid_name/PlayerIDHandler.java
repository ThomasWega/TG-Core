package net.trustgames.core.player.uuid_name;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerIDHandler implements Listener {

    private final PlayerIDFetcher uuidFetcher;
    private final UUIDCache uuidCache;

    public PlayerIDHandler(Core core) {
        this.uuidFetcher = new PlayerIDFetcher(core);
        this.uuidCache = core.getUuidCache();
    }


    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();
        uuidCache.get(playerName, uuid -> uuidFetcher.write(uuid, playerName));
    }
}
