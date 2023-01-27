package net.trustgames.core.npc;

import net.minecraft.server.level.ServerPlayer;
import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listen implements Listener {


    ServerPlayer npc1;
    NPC npc;

    private final Core core;

    public Listen(Core core) {
        this.core = core;
        this.npc = new NPC(core);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        npc1 = npc.create(new Location(Bukkit.getWorld("world"), 40, 78, 17), "");
        npc.addNPCPacket(npc1, player);
        npc.hideName(npc1);

        Bukkit.getScheduler().runTaskLater(core, () -> {
            npc.sendSetNPCSkinPacket(npc1, player, "Tomousek");
            npc.lookNPCPacket(npc1, player, 120, 0);

            Bukkit.getScheduler().runTaskLater(core, () -> npc.hideFromTab(npc1, player), 20);

        }, 60);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        npc.removeNPCPacket(npc1, player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
    }

}
