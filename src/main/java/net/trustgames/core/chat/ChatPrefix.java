package net.trustgames.core.chat;

import net.trustgames.core.Core;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

public class ChatPrefix implements Listener {

    private final Core core;

    public ChatPrefix(Core core) {
        this.core = core;
    }


    // used to apply chat prefixes for players
    @EventHandler
    @Deprecated
    private void chatPrefix(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        FileConfiguration config = core.getConfig();

        String prefix = LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix();


        // get the permission player needs to have to allow to use color codes in chat
        if (player.hasPermission(Objects.requireNonNull(config.getString("chat.allow-colors-permission"))))
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

        // if the primary group of player is default, don't apply any prefix
        if (LuckPermsManager.getPlayerPrimaryGroup(player).equals("default"))
            event.setFormat(ChatColor.translateAlternateColorCodes('&', "&e" + player.getName() + ChatColor.RESET + " ") + event.getMessage());
        else
            event.setFormat(ChatColor.translateAlternateColorCodes('&', prefix + ChatColor.RESET + "&e " + player.getName() + ChatColor.RESET + " ") + event.getMessage());
    }
}
