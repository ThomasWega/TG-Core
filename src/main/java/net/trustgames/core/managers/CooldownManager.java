package net.trustgames.core.managers;

import net.trustgames.core.Core;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CooldownManager implements Listener {

    private final Core core;

    public CooldownManager(Core core) {
        this.core = core;
    }

    private final HashMap<UUID, Long> commandCooldownTime = new HashMap<>();
    private final HashMap<UUID, Long> cooldownMessageTime = new HashMap<>();

    /*
    This method can be called from anywhere and ensures, that the command is not being spammed too much. I already
    have one of this check in the CommandManager, but that allows certain number of commands per second. This
    method allows only one execution of the command per given time. It also ensures that the "don't spam" message
    is not being sent too often to the player.
     */

    public boolean commandCooldown(Player player, Double cooldownTime){
        /*
         if the player is not in the cooldown yet, or if his cooldown expired,
         put him in the hashmap with the new time
        */
        if (!commandCooldownTime.containsKey(player.getUniqueId()) || !isOnCooldown(player, cooldownTime)) {
            commandCooldownTime.put(player.getUniqueId(), System.currentTimeMillis());
        // if the player has a cooldown, send him the wait message
        } else if (isOnCooldown(player, cooldownTime)) {
            sendMessage(player);
            return true;
        }
        return false;
    }

    // method to check if player is on cooldown
    private boolean isOnCooldown(Player player, Double cooldownTime){
        return !(cooldownTime <= (System.currentTimeMillis() - commandCooldownTime.get(player.getUniqueId())) / 1000d);
    }

    // check if the wait message isn't being sent too often to avoid it being too spammy
    private boolean isSpam(Player player) {
        FileConfiguration config = core.getConfig();

        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (cooldownMessageTime.containsKey(player.getUniqueId())) {
            return !(config.getDouble("cooldowns.cooldown-warn-messages-limit-in-seconds") <= (System.currentTimeMillis() - cooldownMessageTime.get(player.getUniqueId())) / 1000d);
            // if the last message doesn't contain the player (meaning he probably didn't receive any wait messages, put him in the map and return false
        } else {
            cooldownMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
            return false;
        }
    }
    

    private void sendMessage(Player player){
        FileConfiguration config = core.getConfig();

        // check if the message is not too spammy
        if (isSpam(player)) return;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("messages.command-spam"))));

        cooldownMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // on player quit, remove his entries from the hashmaps
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        commandCooldownTime.remove(player.getUniqueId());
        cooldownMessageTime.remove(player.getUniqueId());
    }
}
