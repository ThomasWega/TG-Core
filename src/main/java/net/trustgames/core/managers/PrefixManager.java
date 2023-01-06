package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

public class PrefixManager implements Listener {

    private final Core core;

    public PrefixManager(Core core) {
        this.core = core;
        // register the user data change event
        EventBus eventBus = Core.getLuckPerms().getEventBus();
        eventBus.subscribe(core, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event){
        tablistPrefix();
    }

    public static void tablistPrefix(){
        for(Player player : Bukkit.getServer().getOnlinePlayers()){

            // get the prefix the player should have
            String prefix = LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix();

            // if the players primary group is default, don't set any prefix
            if (Objects.equals(LuckPermsManager.getPlayerPrimaryGroup(player), "default")){
                player.playerListName(Component.text(ChatColor.translateAlternateColorCodes('&', player.getName())));
                // if the players primary group is anything else, set the prefix
            } else{
                player.playerListName(Component.text(ChatColor.translateAlternateColorCodes('&',  prefix + "&f " + player.getName())));
            }
        }
    }

    @EventHandler
    private void chatPrefix(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        String prefix = LuckPermsManager.getUser(player).getCachedData().getMetaData().getPrefix();

        if (player.hasPermission("core.knight"))
           event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

        if (LuckPermsManager.getPlayerPrimaryGroup(player).equals("default"))
            event.setFormat(ChatColor.translateAlternateColorCodes('&', "&e" + player.getName() + ChatColor.RESET + " ") + event.getMessage());
        else
            event.setFormat(ChatColor.translateAlternateColorCodes('&', prefix + ChatColor.RESET + "&e " + player.getName() + ChatColor.RESET + " ") + event.getMessage());
    }
}
