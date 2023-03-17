package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.player.data.config.PlayerDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class PlayerDataHandler implements Listener {

    private final Core core;

    public PlayerDataHandler(Core core) {
        this.core = core;
    }


    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();

        PlayerDataFetcher dataFetcher = new PlayerDataFetcher(core, PlayerDataType.UUID);
        dataFetcher.update(uuid, playerName);
    }
}
