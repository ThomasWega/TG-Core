package net.trustgames.core.managers;

import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.commands.messages_commands.MessagesCommandsConfig;
import net.trustgames.core.config.CommandConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Handles cooldowns for chat and commands messages.
 * Can be used by external classes
 */
public class CooldownManager implements Listener {

    private final HashMap<UUID, Long> commandCooldownTime = new HashMap<>();
    private final HashMap<UUID, Long> cooldownMessageTime = new HashMap<>();

    /**
     * Ensures, that the command is not being spammed too much.
     * There is already one of this check in the CommandManager,
     * but that allows certain number of commands per second.
     * This method allows only one execution of the command per given time.
     * It also ensures that the "don't spam" message is not being sent too often to the player.
     * @param uuid UUID of Player to put in a cooldown
     * @param cooldownTime Cooldown time in seconds
     * @return True if the player is on cooldown
     */
    public boolean commandCooldown(UUID uuid, Double cooldownTime){
        /*
         if the player is not in the cooldown yet, or if his cooldown expired,
         put him in the hashmap with the new time
        */
        if (!commandCooldownTime.containsKey(uuid) || !isOnCooldown(uuid, cooldownTime)) {
            commandCooldownTime.put(uuid, System.currentTimeMillis());
        } else if (isOnCooldown(uuid, cooldownTime)) {
            sendMessage(uuid);
            return true;
        }
        return false;
    }

    /**
     * @param uuid UUID of Player to check cooldown on
     * @param cooldownTime Cooldown time in seconds
     * @return if player is on cooldown
     */
    private boolean isOnCooldown(UUID uuid, Double cooldownTime){
        return !(cooldownTime <= (System.currentTimeMillis() - commandCooldownTime.get(uuid)) / 1000d);
    }

    /**
     * check if the wait message isn't being sent too often to avoid it being too spammy
     *
     * @param uuid UUID of Player which the cooldown messages are being sent to
     * @return if the cooldown message is too spammy
     */
    private boolean isSpam(UUID uuid) {
        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (cooldownMessageTime.containsKey(uuid)) {
            return !(MessagesCommandsConfig.WARN_MESSAGES_LIMIT_SEC.getDouble() <= (System.currentTimeMillis() - cooldownMessageTime.get(uuid)) / 1000d);
        } else {
            cooldownMessageTime.put(uuid, System.currentTimeMillis());
            return false;
        }
    }


    /**
     * Send the cooldown messages
     *
     * @param uuid UUID of Player to send the messages to
     */
    private void sendMessage(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;
        if (isSpam(uuid)) return;

        player.sendMessage(CommandConfig.COMMAND_SPAM.getText());

        cooldownMessageTime.put(uuid, System.currentTimeMillis());
    }

    // on player quit, remove player's entries from the hashmaps
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);

        commandCooldownTime.remove(uuid);
        cooldownMessageTime.remove(uuid);
    }
}
