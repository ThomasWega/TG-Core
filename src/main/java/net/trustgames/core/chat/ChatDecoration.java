package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.trustgames.core.chat.config.ChatConfig;
import net.trustgames.core.managers.LuckPermsManager;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles the addition of prefix and colors in the chat
 */
public final class ChatDecoration implements Listener {

    private final Sound sound = Sound.sound(Key.key(
                    "block.note_block.flute"),
            Sound.Source.AMBIENT,
            0.75f, 2f);

    /**
     * Adds the player a prefix and makes sure that the Minecraft new
     * report feature doesn't work and doesn't produce the symbols next to the
     * chat message. Also makes sure to send a modified message if a player is
     * mentioned in the chat.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void decorate(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Component message = setColor(sender, event.originalMessage());

        Component prefix = setPrefix(sender);

        for (Player p : Bukkit.getOnlinePlayers()) {
            // if the player is not mentioned, send him the normal message without colored name
            if (!setMention(sender, p, message, prefix)) {
                p.sendMessage(getMessage(sender, prefix, message));

                logMessage(prefix, sender.getName(), message);
            }
        }
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
    private Component setColor(Player player, Component message) {
        TextColor messageColor = ChatConfig.COLOR.getColor();
        message = player.hasPermission(ChatConfig.ALLOW_COLORS_PERM.value)
                ? ColorUtils.color(message).colorIfAbsent(messageColor)
                : Component.text(ColorUtils.stripColor(message)).color(messageColor);
        return message;
    }

    /**
     * Set colored names in the chat message for the players
     * that were mentioned. Also send them action bar message
     * and play a sound
     *
     * @param sender  The message sender
     * @param p       Player from the loop
     * @param message Chat message that was sent
     * @param prefix  What prefix the player has
     * @return True if mention colors were set
     */
    private boolean setMention(Player sender, Player p, Component message, Component prefix) {
        Set<Player> mentionedPlayers = new HashSet<>();

        message = setColor(sender, message);

        // remove the player name from the message
        String desMsg = ComponentUtils.toString(message)
                .replace(sender.displayName().toString(), "")
                .replaceAll(ChatConfig.COLOR.value, "");
        List<String> split = Arrays.stream(desMsg.split(" ")).toList();

        // check if chat message contains player's name
        if (split.contains(p.getName())) {
            mentionedPlayers.add(p);
        }

        if (mentionedPlayers.contains(p)) {
            List<Component> newMsg = new ArrayList<>();

            TextColor nameColor = ChatConfig.MENTION_COLOR.getColor();
            TextColor messageColor = ChatConfig.COLOR.getColor();
            TextColor messageColorNew;
            // if the word equals player's name, color the name
            for (String s : split) {
                if (s.equalsIgnoreCase(p.getName())) {
                    newMsg.add(Component.text(s).color(nameColor));
                } else {
                    messageColorNew = (ColorUtils.color(s).color() != null) ? ColorUtils.color(s).color() : messageColor;
                    newMsg.add(ColorUtils.color(s).colorIfAbsent(messageColorNew));
                }
            }

            Component msg = Component.join(JoinConfiguration.separator(Component.text(" ")), newMsg);

            // send different types of messages depending on if the player has permission to use color codes
            p.sendMessage(getMessage(sender, prefix, msg));

            logMessage(prefix, sender.getName(), msg);

            p.sendActionBar(ChatConfig.MENTION_ACTIONBAR.formatMessage(sender));
            Audience.audience(p).playSound(sound, Sound.Emitter.self());
            return true;
        }
        return false;
    }

    /**
     * Gets and formats the correct prefix for the player.
     * If player has no prefix, null will be replaced with ""
     *
     * @param player Player to get prefix on
     * @return Player's prefix
     */
    private Component setPrefix(Player player) {
        Component prefix = LuckPermsManager.getPlayerPrefix(player);

        // if player doesn't have any prefix, make sure there is not a space before his name
        if (!prefix.equals(Component.text(""))) {
            prefix = prefix.append(Component.text(" "));
        }
        return prefix;
    }

    /**
     * Get the component message each player will be sent
     *
     * @param player  Player to copy his name on name click
     * @param prefix  Prefix the player has
     * @param message Only the last parts change, depending on if player can use color or not
     * @return Complete Component message
     */
    private Component getMessage(Player player, Component prefix, Component message) {
        return Component.textOfChildren(prefix)
                .append(Component.textOfChildren(
                        player.displayName()
                                .hoverEvent(HoverEvent.showText(Component.text("TO ADD...")))
                                .clickEvent(ClickEvent.suggestCommand(player.getName()))))
                .append(Component.text(" ")
                        .append(message));
    }

    /**
     * Log the message in console without the colors
     *
     * @param prefix     Player's prefix
     * @param playerName Player's name that sent the chat message
     * @param message    Message sent in chat by Player
     */
    private void logMessage(Component prefix, String playerName, Component message) {
        Bukkit.getLogger().log(Level.INFO, ColorUtils.stripColor(
                prefix.append(Component.text(playerName)).append(Component.text(" ")).append(message)));
    }
}
