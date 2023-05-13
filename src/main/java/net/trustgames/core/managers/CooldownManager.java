package net.trustgames.core.managers;

import net.trustgames.toolkit.config.cooldown.CooldownConfig;
import net.trustgames.toolkit.config.cooldown.CooldownValueConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles cooldowns for chat and commands messages.
 * Can be used by external classes
 */
public final class CooldownManager implements Listener {

    private final ConcurrentHashMap<UUID, Long> commandCooldownTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> cooldownMessageTime = new ConcurrentHashMap<>();
    private final double cooldownTime;

    public CooldownManager(double cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    /**
     * Ensures, that the action is not being spammed too much.
     * There is already one of this check in the CommandCooldownManager,
     * but that allows certain number of commands per second.
     * This method allows only one execution of the action per given time.
     * It also ensures that the "don't spam" message is not being sent too often to the player.
     *
     * @param player Player to put a cooldown on
     * @return True if the player is on cooldown
     */
    public boolean handleCooldown(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        /*
         if the player is not in the cooldown yet, or if his cooldown expired,
         put him in the hashmap with the new time
        */
        if (!commandCooldownTime.containsKey(uuid) || !isOnCooldown(uuid, cooldownTime)) {
            commandCooldownTime.put(uuid, System.currentTimeMillis());
        } else if (isOnCooldown(uuid, cooldownTime)) {
            sendMessage(player);
            return true;
        }
        return false;
    }

    /**
     * @param uuid   UUID of the Player to check cooldown on
     * @param cooldownTime CooldownManager time in seconds
     * @return if player is on cooldown
     */
    private boolean isOnCooldown(@NotNull UUID uuid, double cooldownTime) {
        return !(cooldownTime <= (System.currentTimeMillis() - commandCooldownTime.get(uuid)) / 1000d);
    }

    /**
     * check if the wait message isn't being sent too often to avoid it being too spammy
     *
     * @param uuid UUID of the Player which the cooldown messages are being sent to
     * @return if the cooldown message is too spammy
     */
    private boolean isSpam(@NotNull UUID uuid) {
        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (cooldownMessageTime.containsKey(uuid)) {
            return !(CooldownValueConfig.WARN_MESSAGES_LIMIT_SEC.getValue()
                    <= (System.currentTimeMillis() - cooldownMessageTime.get(uuid)) / 1000d);
        } else {
            cooldownMessageTime.put(uuid, System.currentTimeMillis());
            return false;
        }
    }


    /**
     * Send the cooldown messages
     *
     * @param player Player to send the messages to
     */
    private void sendMessage(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (isSpam(uuid)) return;

        player.sendMessage(CooldownConfig.SPAM.getFormatted());

        cooldownMessageTime.put(uuid, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        commandCooldownTime.remove(uuid);
        cooldownMessageTime.remove(uuid);
    }
}
