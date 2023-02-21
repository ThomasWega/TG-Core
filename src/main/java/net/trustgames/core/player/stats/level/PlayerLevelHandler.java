package net.trustgames.core.player.stats.level;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerLevelHandler implements Listener {

    private final Core core;

    public PlayerLevelHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);
        PlayerLevel playerLevel = new PlayerLevel(core, uuid);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!player.isOnline()) cancel();

                player.sendActionBar(Component.text(
                        playerLevel.getXp() + "/" + playerLevel.getThreshold(playerLevel.getLevel())
                                + " - " + playerLevel.getLevel() + " (" + Math.round(playerLevel.getProgress(playerLevel.getLevel()) * 100) + "%)"));
                playerLevel.addXp(uuid, 10);

                double levelProgress = playerLevel.getProgress(playerLevel.getXp());
                player.setExp(((float) levelProgress));
                player.setLevel(playerLevel.getLevel());
            }
        }.runTaskTimer(core, 10, 60);
    }
}
