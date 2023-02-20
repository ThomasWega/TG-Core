package net.trustgames.core.stats.level;

import net.kyori.adventure.text.Component;
import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LevelHandler implements Listener {

    private final Core core;

    public LevelHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PlayerLevel playerLevel = new PlayerLevel();

        Bukkit.getScheduler().runTaskTimer(core, new Runnable() {
            @Override
            public void run() {
                player.sendActionBar(Component.text(
                        playerLevel.xp + "/" + playerLevel.getThreshold(playerLevel.level)
                                + " - " + playerLevel.level + " (" + Math.round(playerLevel.getProgress(playerLevel.xp) * 100) + "%)"));
                playerLevel.addXp(10);

                double levelProgress = playerLevel.getProgress(playerLevel.xp);
                player.setExp(((float) levelProgress));
                player.setLevel(playerLevel.level);
            }
        }, 10, 10);
    }
}
