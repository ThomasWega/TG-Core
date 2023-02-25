package net.trustgames.core.player.data.level;

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

                playerLevel.getXp(xp -> playerLevel.getLevel(level -> {
                    int nextLevelThreshold = playerLevel.getThreshold(level + 1);
                    float progress = playerLevel.getProgress(xp);
                    Component actionBar = Component.text()
                            .append(Component.text(xp + "/" + nextLevelThreshold))
                            .append(Component.text(" - "))
                            .append(Component.text(level))
                            .append(Component.text(" ("))
                            .append(Component.text(Math.round(progress * 100) + "%"))
                            .append(Component.text(")"))
                            .build();

                    player.sendActionBar(actionBar);

                    float levelProgress = playerLevel.getProgress(xp);
                    player.setExp(levelProgress);
                    player.setLevel(level);

                    playerLevel.addXp(10);
                }));
            }
        }.runTaskTimer(core, 10, 60);
    }
}
