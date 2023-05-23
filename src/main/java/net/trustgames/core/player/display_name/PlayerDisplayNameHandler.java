package net.trustgames.core.player.display_name;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerDisplayNameHandler implements Listener {

    public PlayerDisplayNameHandler(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.displayName(PlayerDisplayNameConfig.DISPLAY_NAME.getDisplayName(player));
    }
}
