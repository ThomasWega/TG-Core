package net.trustgames.core.player;

import net.kyori.adventure.text.Component;
import net.trustgames.core.chat.config.ChatConfig;
import net.trustgames.core.tablist.TablistTeams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerHandler implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.displayName(Component.text(player.getName()).color(
                ChatConfig.NAME_COLOR.getColor()));

        TablistTeams.addToTeam(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        TablistTeams.removeFromTeam(event.getPlayer());
    }
}
