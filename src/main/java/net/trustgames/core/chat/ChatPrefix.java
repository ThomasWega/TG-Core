package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trustgames.core.Core;
import net.trustgames.core.managers.ColorManager;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

/**
 * Handles the addition of prefix and colors in the chat
 */
public class ChatPrefix implements Listener {

    private final Core core;

    public ChatPrefix(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        FileConfiguration config = core.getConfig();

        Player player = event.getPlayer();
        String playerDisplayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        String prefix = LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix();

        // get the permission player needs to have to allow to use color codes in chat
        String path = "chat.allow-colors-permission";
        if (player.hasPermission(Objects.requireNonNull(config.getString(path,
                "String on path " + path + " wasn't found in config!"))))

            message = ColorManager.translateColors(event.originalMessage().toString());

        if (prefix == null)
            core.getServer().broadcast(Component.text(ColorManager.translateColors
                    ("&e" + playerDisplayName + ChatColor.RESET + " ") + message));
        else
            core.getServer().broadcast(Component.text(ColorManager.translateColors
                    (prefix + ChatColor.RESET + "&e " + playerDisplayName + ChatColor.RESET + " ") + message));

        event.setCancelled(true);
    }
}
