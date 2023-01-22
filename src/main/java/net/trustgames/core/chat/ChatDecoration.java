package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trustgames.core.Core;
import net.trustgames.core.managers.ColorManager;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

/**
 * Handles the addition of prefix and colors in the chat
 */
public class ChatDecoration {

    private final Core core;

    public ChatDecoration(Core core) {
        this.core = core;
    }

    /**
     * Adds the player a prefix and makes sure that the Minecraft new
     * report feature doesn't work and doesn't produce the symbols next to the
     * chat message. Also makes sure to send a modified message if a player is
     * mentioned in the chat.
     *
     * @param event the main AsyncChatEvent
     */
    public void decorate(AsyncChatEvent event) {
        FileConfiguration config = core.getConfig();

        Player player = event.getPlayer();
        String playerDisplayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());

        Component message = setColors(player, event.originalMessage());
        String path = "chat.default-chat-color";
        String messageColor = Objects.requireNonNull(config.getString(path), "String on path " + path + " wasn't found in config!");

        String prefix = setPrefix(player);

        for (Player p : Bukkit.getOnlinePlayers()) {
            // if the player is not mentioned, send him the normal message without colored name
            if (!setMention(p, message, prefix, playerDisplayName, event, messageColor)) {
                p.sendMessage(getMessage(player, prefix, playerDisplayName, messageColor, message));

                logMessage(prefix, playerDisplayName, message);
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
    private Component setColors(Player player, Component message){
        if (allowColors(player))
            message = ColorManager.color(PlainTextComponentSerializer.plainText().serialize(message));
        return message;
    }

    private boolean allowColors(Player player){
        FileConfiguration config = core.getConfig();
        // get the permission player needs to have to allow to use color codes in chat
        String path = "chat.allow-colors-permission";
        return player.hasPermission(Objects.requireNonNull(config.getString(path,
                "String on path " + path + " wasn't found in config!")));
    }

    /**
     * Set colored names in the chat message for the players
     * that were mentioned. Also send them action bar message
     * and play a sound
     *
     * @param p Player from the loop
     * @param message Chat message that was sent
     * @param prefix What prefix the player has
     * @param playerDisplayName Player's display name who sent the message
     * @param event The main AsyncChatEvent
     * @return True if mention colors were set
     */
    private boolean setMention(Player p, Component message, String prefix, String playerDisplayName, AsyncChatEvent event, String messageColor){
        FileConfiguration config = core.getConfig();

        Set<Player> mentionedPlayers = new HashSet<>();
        Player player = event.getPlayer();

        // remove the player name from the message
        String desMsg = LegacyComponentSerializer.legacyAmpersand().serialize(message)
                .replace(player.displayName().toString(), "");
        List<String> split = Arrays.stream(desMsg.split(" ")).toList();

        // check if chat message contains player's name
        for (Player player1 : Bukkit.getOnlinePlayers()){
            if (split.contains(p.getName())){
                mentionedPlayers.add(player1);
            }
        }

        if (mentionedPlayers.contains(p)) {
            List<Component> newMsg = new ArrayList<>();

            String path1 = "chat.mention.color";
            String nameColor = Objects.requireNonNull(config.getString(path1),
                    "String on path " + path1 + " wasn't found in config!");

            for (String s : split) {
                if (s.equalsIgnoreCase(p.getName())) {
                    newMsg.add(ColorManager.color(nameColor + s + messageColor));
                }else{
                    newMsg.add(ColorManager.color(messageColor).append(Component.text(s)));
                }
            }

            Component msg = Component.join(JoinConfiguration.separator(Component.text(" ")), newMsg);

            p.sendMessage(allowColors(player)
                    ? getMessage(player, prefix, playerDisplayName, messageColor, ColorManager.color(
                            LegacyComponentSerializer.legacyAmpersand().serialize(msg)))
                    : getMessage(player, prefix, playerDisplayName, messageColor, msg));

            logMessage(prefix, playerDisplayName, msg);

            String path2 = "messages.mention.action-bar";
            p.sendActionBar(ColorManager.color(Objects.requireNonNull(config.getString(path2),
                    "String on path " + path2 + " wasn't found in config!")));
            p.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2);

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
    private String setPrefix(Player player){
        String prefix = LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix() + " ";

        // if player doesn't have any prefix, make sure there is not a space before his name
        if (LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix() == null) {
            prefix = "";
        }
        return prefix;
    }

    private Component getMessage(Player player, String prefix, String playerDisplayName,
                                 String messageColor, Component lastPartOfMessage){
        return ColorManager.color(prefix + ChatColor.RESET)
                .append(ColorManager.color( "&e" + playerDisplayName + ChatColor.RESET)
                        .clickEvent(ClickEvent.suggestCommand(player.getName())))
                .append(ColorManager.color(messageColor + " ")
                        .append(lastPartOfMessage));
    }

    private void logMessage(String prefix, String playerDisplayName, Component message){
        // log the message in console without the colors
        Bukkit.getLogger().log(Level.INFO, PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacy('&')
                        .deserialize(prefix + playerDisplayName + " ").append(message)));
    }
}
