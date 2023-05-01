package net.trustgames.core.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.toolkit.config.chat.ChatConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerHandler implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.displayName(
                ColorUtils.color(ChatConfig.NAME_COLOR.value + player.getName())
                        .hoverEvent(HoverEvent.showText(Component.text("TO ADD...")))
                        .clickEvent(ClickEvent.suggestCommand(player.getName()))
        );
    }
}
