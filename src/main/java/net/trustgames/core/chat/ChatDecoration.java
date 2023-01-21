package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
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
        String playerDisplayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());

        String message = setColors(player, event.message());
        String path = "chat.default-chat-color";
        String messageColor = ColorManager.color(Objects.requireNonNull(config.getString(path), "String on path " + path + " wasn't found in config!"));

        String prefix = setPrefix(player);

        for (Player p : Bukkit.getOnlinePlayers()) {
            // if the player is not mentioned, send him the normal message without colored name
            if (!setMention(p, message, prefix, playerDisplayName, event, messageColor)) {
                p.sendMessage(ColorManager.color
                        (prefix + ChatColor.RESET + "&e" + playerDisplayName + ChatColor.RESET + " ") + messageColor + message);
                // log the message in console without the colors
                Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor(prefix + playerDisplayName + " " + message)
                        .replaceAll("&.", ""));
            }
        }
        event.setCancelled(true);
    }

    /**
     * Allow player to use colors in chat, if he has the permission
     *
     * @param player Player to check on
     * @param message Message he sent
     * @return Colored message if player has permission
     */
    private String setColors(Player player, Component message){
        FileConfiguration config = core.getConfig();

        String msg = PlainTextComponentSerializer.plainText().serialize(message);

        // get the permission player needs to have to allow to use color codes in chat
        String path = "chat.allow-colors-permission";
        if (player.hasPermission(Objects.requireNonNull(config.getString(path,
                "String on path " + path + " wasn't found in config!"))))
            msg = ColorManager.color(msg);
        return msg;
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
    private boolean setMention(Player p, String message, String prefix, String playerDisplayName, AsyncChatEvent event, String messageColor){
        FileConfiguration config = core.getConfig();

        Set<Player> mentionedPlayers = new HashSet<>();
        Player player = event.getPlayer();
        // remove the player name from the message
        //noinspection ResultOfMethodCallIgnored
        message.replace(player.displayName().toString(), "");
        List<String> split = Arrays.stream(message.split(" ")).toList();

        // check if chat message contains player's name
        for (Player player1 : Bukkit.getOnlinePlayers()){
            if (split.contains(p.getName())){
                mentionedPlayers.add(player1);
            }
        }

        if (mentionedPlayers.contains(p)) {
            List<String> newMsg = new ArrayList<>();

            String path1 = "chat.mention.color";
            String color = ColorManager.color(Objects.requireNonNull(config.getString(path1),
                    "String on path " + path1 + " wasn't found in config!"));

            for (String s : split) {
                if (s.equalsIgnoreCase(p.getName())) {
                    s = color + s + ChatColor.RESET;
                }
                newMsg.add(s);
            }

            String msg = String.join(" ", newMsg);

            p.sendMessage(ColorManager.color
                    (prefix + ChatColor.RESET + "&e" + playerDisplayName + ChatColor.RESET + " ") + messageColor + msg);
            // log the message in console without the colors
            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor(prefix + playerDisplayName + " " + msg)
                    .replaceAll("&.", ""));

            String path2 = "messages.mention.action-bar";
            p.sendActionBar(Component.text(ColorManager.color(Objects.requireNonNull(config.getString(path2),
                    "String on path " + path2 + " wasn't found in config!"))));
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
}
