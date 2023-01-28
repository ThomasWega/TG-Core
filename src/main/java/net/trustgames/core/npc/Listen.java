package net.trustgames.core.npc;

import net.minecraft.server.level.ServerPlayer;
import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
        npc.add(npc1, player);
        npc.hideName(npc1);

        Bukkit.getScheduler().runTaskLater(core, () -> {

            String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY3NDkwNTI4NDk4NCwKICAicHJvZmlsZUlkIiA6ICJmZDIwMGYwMDE4OTI0NzgxODI5OWIzZjE5Yzc4Y2E3MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0dXNnIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY1MDYxMzkwN2YwOWM2MTcxOWJjMjMxOTE3ZjhhYWIxZmNmOWY4MjEyYmYyZGU1MjdiZTU3MzZkOGE1NjA2OGIiCiAgICB9CiAgfQp9";
            String signature = "gH0t+McOqT3MFIuZ14kIz4WxRN6sGCuWbbHgpTD6pvMF0wmn3qNd4wRSfvrsMwgqgvp1T1pflVY8KIBmiKcO41KPaZnOsgSA1Cm84VVconq91VwlWJwikMtSaoXXu8OP8tap03Yi6tpTQjbYEEiIdRPhhqOnd45/YnPoPjyW5si8BLs7ttH7hnY+tx0Zzq1pJcWgyoODzEhJqaJq+PBOZ43kmvDCZii8vmDS28XdCRiI+k5hBVaJEyI48eQv75rPPCtA7YYjIAwQ9roewMDZmtJflvKvRUvSbo52jzEMvTbj+GDdysH4soM9S1Lq3013dr1M3F1v5eSZ/eA503/K3wUWmDaoaECKbLBtkmx/op2ZAWXP9l2y/0uRDQpO7JVlPRpEgJ9Zv8AlOw9EyTraQZS8wS99IvZ27qtnZ09wG/R1YAIni93Xna+zx/f3yEZVe61XCiYC6rrwepZ8IAake5jk1bCOJs0kh0q5iCvXNu/njc9MRUPZffCfR15iGyW5WbJKt4MJZq4VLIaeEKpPtZ/gKKQRlu+UZ5bYz0QVrYYxq42Bt/C/69IcedCDWSfN+6wZoJ4oJzK4XZuQrgjqc2Iz4DAc4BnW5tpFkixgxxZ58+zE9m0VGOvh2HvhM1ogeDKIruyB5RJwctUxLELQwqoH76jWH0MC2QljF9ei3ig=";
            npc.skin(npc1, player, texture, signature);
            npc.look(npc1, player, 130, 0, true);

            Bukkit.getScheduler().runTaskLater(core, () -> npc.hideTab(npc1, player), 20);

        }, 60);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        npc.remove(npc1, player);
    }
}
