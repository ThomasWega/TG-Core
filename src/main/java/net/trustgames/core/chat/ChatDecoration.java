package net.trustgames.core.chat;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.Core;
import net.trustgames.core.managers.LuckPermsManager;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.ComponentUtils;
import net.trustgames.toolkit.config.chat.ChatConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles the addition of prefix and colors in the chat
 */
public final class ChatDecoration implements Listener {

    public ChatDecoration(Core core) {
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    private final Sound sound = Sound.sound(Key.key(
                    "block.note_block.flute"),
            Sound.Source.AMBIENT,
            0.75f, 2f);

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
        TextColor messageColor = ColorUtils.color(ChatConfig.CHAT_COLOR.getFormatted()).color();
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
     * @param sender  The message sender
     * @param loop    Player from the loop
     * @param originalMessage Chat message that was sent
     * @return True if mention colors were set
     */
    private boolean setMention(@NotNull Player sender,
                               @NotNull Player loop,
                               @NotNull Component originalMessage,
                               @NotNull Component preMessageField) {
        Set<Player> mentionedPlayers = new HashSet<>();
        String loopName = loop.getName();
        originalMessage = setColor(sender, originalMessage);

        // remove the player name from the message
        String strMsg = ComponentUtils.toString(originalMessage);
        List<String> split = Arrays.stream(strMsg.split(" ")).toList();

        // check if chat message contains player's name
        split.forEach(s -> {
            String stripped = ColorUtils.stripColor(Component.text(s));
            if (stripped.equalsIgnoreCase(loopName))
                mentionedPlayers.add(loop);
        });

        if (mentionedPlayers.contains(loop)) {
            List<Component> newMsg = new ArrayList<>();

            TextColor nameColor = ColorUtils.color(ChatConfig.MENTION_COLOR.getValue()).color();
            TextColor chatColor = ColorUtils.color(ChatConfig.CHAT_COLOR.getValue()).color();
            // if the word equals player's name, color the name
            TextColor lastColor = null;

            for (String s : split) {
                String stripped = ColorUtils.stripColor(Component.text(s));
                if (stripped.contains(loopName)) {
                    newMsg.add(Component.text(stripped, nameColor));
                    /*
                    loops through the last message all the way to the last one,
                    until a color code is found. If one is found, it is used to set the
                    same color after the colored mention name
                     */
                    for (int i = (newMsg.size() - 2); i >= 0; i--) {
                        String idk = ComponentUtils.toString(newMsg.get(i));
                        int index = idk.lastIndexOf("&");
                        if (index != -1) {
                            lastColor = ColorUtils.color(idk.substring(index)).color();
                        }
                    }
                } else {
                    TextColor currentColor = ColorUtils.color(s).color();
                    // save if a color was used
                    if (currentColor != null) {
                        lastColor = currentColor;
                    }
                    // if any color was used, color the following text as well
                    if (lastColor != null) {
                        s = "&" + lastColor.asHexString() + s;
                    }
                    newMsg.add(ColorUtils.color(s).colorIfAbsent(chatColor));
                }
            }

            Component msg = Component.join(JoinConfiguration.separator(Component.text(" ")), newMsg);

            // send different types of messages depending on if the player has permission to use color codes
            loop.sendMessage(preMessageField.append(msg));

            loop.sendActionBar(ChatConfig.MENTION_ACTIONBAR.formatMessage(
                    sender.getName(), LuckPermsManager.getPlayerPrefix(sender))
            );
            Audience.audience(loop).playSound(sound, Sound.Emitter.self());
            return true;
        }
        return false;
    }

    /**
     * @param player  Player to resolve the data for
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
     * @param fullMessage  The chat message also containing name
     */
    private void logMessage(@NotNull Component fullMessage) {
        Bukkit.getLogger().log(Level.INFO, ColorUtils.stripColor(fullMessage));
    }
}
