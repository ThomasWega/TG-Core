package net.trustgames.core.managers;

import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CooldownConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Handles cooldowns for chat and commands messages.
 * Can be used by external classes
 */
public final class CooldownManager implements Listener {

    private final HashMap<String, Long> commandCooldownTime = new HashMap<>();
    private final HashMap<String, Long> cooldownMessageTime = new HashMap<>();

    /**
     * Ensures, that the command is not being spammed too much.
     * There is already one of this check in the CommandManager,
     * but that allows certain number of commands per second.
     * This method allows only one execution of the command per given time.
     * It also ensures that the "don't spam" message is not being sent too often to the player.
     *
     * @param player       Player to put a cooldown on
     * @param cooldownTime Cooldown time in seconds
     * @return True if the player is on cooldown
     */
    public boolean commandCooldown(@NotNull Player player, long cooldownTime) {
        String playerName = player.getName();
        /*
         if the player is not in the cooldown yet, or if his cooldown expired,
         put him in the hashmap with the new time
        */
        if (!commandCooldownTime.containsKey(playerName) || !isOnCooldown(playerName, cooldownTime)) {
            commandCooldownTime.put(playerName, System.currentTimeMillis());
        } else if (isOnCooldown(playerName, cooldownTime)) {
            sendMessage(player);
            return true;
        }
        return false;
    }

    /**
     * @param playerName   Name of the Player to check cooldown on
     * @param cooldownTime Cooldown time in seconds
     * @return if player is on cooldown
     */
    private boolean isOnCooldown(@NotNull String playerName, long cooldownTime) {
        return !(cooldownTime <= (System.currentTimeMillis() - commandCooldownTime.get(playerName)) / 1000d);
    }

    /**
     * check if the wait message isn't being sent too often to avoid it being too spammy
     *
     * @param playerName Name of the Player which the cooldown messages are being sent to
     * @return if the cooldown message is too spammy
     */
    private boolean isSpam(@NotNull String playerName) {
        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (cooldownMessageTime.containsKey(playerName)) {
            return !(CooldownConfig.WARN_MESSAGES_LIMIT_SEC.value
                    <= (System.currentTimeMillis() - cooldownMessageTime.get(playerName)) / 1000d);
        } else {
            cooldownMessageTime.put(playerName, System.currentTimeMillis());
            return false;
        }
    }


    /**
     * Send the cooldown messages
     *
     * @param player Player to send the messages to
     */
    private void sendMessage(@NotNull Player player) {
        String playerName = player.getName();
        if (isSpam(playerName)) return;

        player.sendMessage(CommandConfig.COMMAND_SPAM.getText());

        cooldownMessageTime.put(playerName, System.currentTimeMillis());
    }

    // on player quit, remove player's entries from the hashmaps
    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        commandCooldownTime.remove(playerName);
        cooldownMessageTime.remove(playerName);
    }
}
