package net.trustgames.core.managers.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.config.chat.ChatConfig;
import net.trustgames.core.managers.LuckPermsManager;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles the addition of prefix and colors in the chat
 */
public class ChatDecoration implements Listener {

    /**
     * Adds the player a prefix and makes sure that the Minecraft new
     * report feature doesn't work and doesn't produce the symbols next to the
     * chat message. Also makes sure to send a modified message if a player is
     * mentioned in the chat.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void decorate(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);
        Component message = setColor(player, event.originalMessage());
        System.out.println(PlainTextComponentSerializer.plainText().serialize(message));

        Component prefix = setPrefix(uuid);

        for (Player p : Bukkit.getOnlinePlayers()) {
            // if the player is not mentioned, send him the normal message without colored name
            if (!setMention(p, message, prefix, event)) {
                p.sendMessage(getMessage(player, prefix, message));

                logMessage(prefix, player.getName(), message);
            }
        }
        event.setCancelled(true);
    }

    /**
     * If the player has the correct permission, it will
     * make his message colored if he wrote any color codes
     *
     * @param player Player to check on
     * @param message Message he sent
     * @return Colored message if player has permission
     */
    private Component setColor(Player player, Component message){
        TextColor messageColor = ChatConfig.COLOR.getColor();
        message = player.hasPermission(ChatConfig.ALLOW_COLORS_PERM.getRaw())
                ? ColorUtils.color(message).colorIfAbsent(messageColor)
                : Component.text(ColorUtils.stripColor(message)).color(messageColor);
        return message;
    }

    /**
     * Set colored names in the chat message for the players
     * that were mentioned. Also send them action bar message
     * and play a sound
     *
     * @param p Player from the loop
     * @param message Chat message that was sent
     * @param prefix What prefix the player has
     * @param event The main AsyncChatEvent
     * @return True if mention colors were set
     */
    private boolean setMention(Player p, Component message, Component prefix, AsyncChatEvent event){
        Set<Player> mentionedPlayers = new HashSet<>();
        Player player = event.getPlayer();

        message = setColor(player, message);

        // remove the player name from the message
        String desMsg = ComponentUtils.toString(message)
                .replace(player.displayName().toString(), "")
                .replaceAll(ChatConfig.COLOR.getRaw(), "");
        List<String> split = Arrays.stream(desMsg.split(" ")).toList();

        System.out.println(desMsg);

        // check if chat message contains player's name
        if (split.contains(p.getName())){
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
                } else{
                    messageColorNew = (ColorUtils.color(s).color() != null) ? ColorUtils.color(s).color() : messageColor;
                    newMsg.add(ColorUtils.color(s).colorIfAbsent(messageColorNew));
                }
            }

            Component msg = Component.join(JoinConfiguration.separator(Component.text(" ")), newMsg);
            player.sendMessage(msg);

            // send different types of messages depending on if the player has permission to use color codes
            p.sendMessage(getMessage(player, prefix, msg));

            logMessage(prefix, player.getName(), msg);

            p.sendActionBar(ChatConfig.MENTION_ACTIONBAR.getText());
            p.playSound(player, Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.75f, 2);

            return true;
        }
        return false;
    }

    /**
     * Gets and formats the correct prefix for the player.
     * If player has no prefix, null will be replaced with ""
     *
     * @param uuid UUID of Player to get prefix on
     * @return Player's prefix
     */
    private Component setPrefix(UUID uuid){
        Component prefix = LuckPermsManager.getPlayerPrefix(uuid);

        // if player doesn't have any prefix, make sure there is not a space before his name
        if (!prefix.equals(Component.text(""))) {
            prefix = prefix.append(Component.text(" "));
        }
        return prefix;
    }

    /**
     * Get the component message each player will be sent
     *
     * @param player Player to copy his name on name click
     * @param prefix Prefix the player has
     * @param message Only the last parts change, depending on if player can use color or not
     * @return Complete Component message
     */
    private Component getMessage(Player player, Component prefix, Component message){
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
     * @param prefix Player's prefix
     * @param playerName Player's name that sent the chat message
     * @param message Message sent in chat by Player
     */
    private void logMessage(Component prefix, String playerName, Component message){
        Bukkit.getLogger().log(Level.INFO, ColorUtils.stripColor(
                prefix.append(Component.text(playerName)).append(Component.text(" ")).append(message)));
    }
}
