package net.trustgames.core.chat;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.Core;
import net.trustgames.toolkit.utils.ColorUtils;
import net.trustgames.toolkit.config.chat.ChatConfig;
import net.trustgames.toolkit.managers.permission.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Handles the addition of prefix and colors in the chat
 */
public final class ChatDecoration implements Listener {

    private final Sound sound = Sound.sound(Key.key(
                    "block.note_block.flute"),
            Sound.Source.AMBIENT,
            0.75f, 2f);

    public ChatDecoration(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    /**
     * Adds the player a prefix and makes sure that the Minecraft new
     * report feature doesn't work and doesn't produce the symbols next to the
     * chat message. Also makes sure to send a modified message if a player is
     * mentioned in the chat.
     *
     * @param event the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void decorate(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Component message = setColor(sender, event.originalMessage());

        Component preMessageField = getPreMessageField(sender);
        Component fullMessage = preMessageField.append(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            // if the player is not mentioned, send him the normal message without colored name
            if (!setMention(sender, p, message, preMessageField)) {
                p.sendMessage(fullMessage);
            }
        }
        logMessage(fullMessage);
        event.setCancelled(true);
    }

    /**
     * If the player has the correct permission, it will
     * make his message colored if he wrote any color codes
     *
     * @param player  Player to check on
     * @param message Message he sent
     * @return Colored message if player has permission
     */
    private Component setColor(@NotNull Player player, @NotNull Component message) {
        TextColor messageColor = ChatConfig.CHAT_COLOR.getColor();
        message = player.hasPermission(ChatConfig.ALLOW_COLORS_PERM.getValue())
                ? ColorUtils.color(message).colorIfAbsent(messageColor)
                : Component.text(ColorUtils.stripColor(message), messageColor);
        return message;
    }

    /**
     * Set colored names in the chat message for the players
     * that were mentioned. Also send them action bar message
     * and play a sound
     *
     * @param sender          The message sender
     * @param loop            Player from the loop
     * @param originalMessage Chat message that was sent
     * @return True if mention colors were set
     */
    private boolean setMention(@NotNull Player sender,
                               @NotNull Player loop,
                               @NotNull Component originalMessage,
                               @NotNull Component preMessageField) {
        String loopName = loop.getName();
        originalMessage = setColor(sender, originalMessage);

        // replace the playerName with his colored name
        Component newMsg = originalMessage.replaceText(builder -> builder
                .match(Pattern.compile("\\b" + loopName + "\\b", Pattern.CASE_INSENSITIVE))
                .replacement(Component.text(loopName, ChatConfig.MENTION_COLOR.getColor()))
        );

        // if nothing changed the player was not mentioned
        if(newMsg.equals(originalMessage)) {
            return false;
        }

        // send a different message (with his name colored) to the mentioned player
        loop.sendMessage(preMessageField.append(newMsg));

        loop.sendActionBar(ChatConfig.MENTION_ACTIONBAR.formatMessage(
                sender.getName(),
                ColorUtils.color(LuckPermsManager.getOnlinePlayerPrefix(sender.getUniqueId())))
        );

        Audience.audience(loop).playSound(sound, Sound.Emitter.self());

        return true;
    }

    /**
     * @param player Player to resolve the data for
     * @return Get the component that will be before the message
     * (eg. PREFIX NAME:)
     */
    private Component getPreMessageField(@NotNull Player player) {
        return MiniMessage.miniMessage().deserialize(
                "<tg_player_level> <tg_player_prefix_spaced><player_displayname> ",
                MiniPlaceholders.getAudiencePlaceholders(player));
    }

    /**
     * Log the message in console without the colors
     *
     * @param fullMessage The chat message also containing name
     */
    private void logMessage(@NotNull Component fullMessage) {
        Bukkit.getLogger().log(Level.INFO, ColorUtils.stripColor(fullMessage));
    }
}
