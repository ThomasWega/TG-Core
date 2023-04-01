package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.trustgames.core.chat.config.ChatConfig;
import net.trustgames.core.config.CorePermissionConfig;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Limit the time player can send the next message. Each permission can have different times set.
 * If the message is the same as the last one, the timeout can be longer.
 */
public final class ChatLimiter implements Listener {
    private final HashMap<String, PlayerChatCooldown> cooldowns = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void limit(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CorePermissionConfig.STAFF.permission)) return;
        String playerName = player.getName();

        // add the player if not yet contained with new cooldown
        cooldowns.computeIfAbsent(playerName, name -> new PlayerChatCooldown(player));

        PlayerChatCooldown cooldown = cooldowns.get(player.getName());
        String playerMessage = ColorUtils.stripColor(event.originalMessage());
        boolean same = cooldown.isSameMessage(playerMessage);
        if (cooldown.isOnCooldown(same)) {
            event.setCancelled(true);
            sendMessage(player, same);
        } else {
            cooldown.setLastMessageTime(System.currentTimeMillis());
        }
    }

    private void sendMessage(@NotNull Player player, boolean sameMessage) {
        PlayerChatCooldown cooldown = cooldowns.get(player.getName());

        ChatConfig unformattedMessage = sameMessage ? ChatConfig.ON_SAME_COOLDOWN : ChatConfig.ON_COOLDOWN;
        Component message = unformattedMessage.addComponent(Component.text(
                new DecimalFormat("0.0").format(cooldown.getWaitTime(sameMessage))));

        player.sendMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getName());
    }
}
