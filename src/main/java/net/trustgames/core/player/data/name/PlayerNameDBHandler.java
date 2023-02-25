package net.trustgames.core.player.data.name;

import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.config.database.player_data.PlayerDataTypes;
import net.trustgames.core.player.data.PlayerDataFetcher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * This class checks if the player name in the database is the same as the last time the player joined
 */
public class PlayerNameDBHandler implements Listener {

    private final Core core;

    public PlayerNameDBHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);
        PlayerDataFetcher playerDataFetcher = new PlayerDataFetcher(core, uuid);

        playerDataFetcher.update(PlayerDataTypes.PLAYER_NAME, player.getName());
    }
}
