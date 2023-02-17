package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.trustgames.core.chat.config.ChatConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerManager implements Listener {

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        setNameColor(player);
    }

    /**
     * Changes the display name color of the player to
     * the config value
     *
     * @param player Player to change name color on
     */
    public static void setNameColor(Player player){
        player.displayName(Component.text(player.getName()).color(ChatConfig.NAME_COLOR.getColor()));
    }
}
