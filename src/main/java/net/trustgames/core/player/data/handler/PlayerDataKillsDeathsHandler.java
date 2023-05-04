package net.trustgames.core.player.data.handler;

import net.trustgames.core.Core;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.database.player.data.PlayerData;
import net.trustgames.toolkit.database.player.data.config.PlayerDataType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDataKillsDeathsHandler implements Listener {

    private final Toolkit toolkit;

    public PlayerDataKillsDeathsHandler(Core core) {
        this.toolkit = core.getToolkit();
        Bukkit.getServer().getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    private void onKillAndDeath(PlayerDeathEvent event){
        Player dead = event.getPlayer();
        Player killer = dead.getKiller();

        onPlayerDeath(dead);
        if (killer != null){
            onPlayerKill(killer);
        }
    }

    private void onPlayerKill(@NotNull Player killer) {
        new PlayerData(toolkit, killer.getUniqueId(), PlayerDataType.KILLS).addData(1);
    }

    private void onPlayerDeath(@NotNull Player death) {
        new PlayerData(toolkit, death.getUniqueId(), PlayerDataType.DEATHS).addData(1);
    }
}
