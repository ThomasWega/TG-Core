package net.trustgames.core.managers;

import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.config.CommandConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Handles the command cooldowns
 */
public class CommandManager implements Listener {

    private final HashMap<UUID, Long> commandCooldown = new HashMap<>();

    int i = 1;

    /** limit the number of commands player can send, to avoid spamming.
    it checks, if the player is already in the hashmap, in case he is not,
    it puts him there. If he is already in the hashmap, it checks if the
    current time - the time in hashmap is less than a second. If it's less
    than a second, it checks, if "i" is more than specified value (default is 10).
    If it's more, that means it's too spammy and the player used a command more times
    than the maximum allowed number in config. If "i" is less than the value specified
    in the config.yml, it just adds +1 to "i". If neither of these checks are valid, meaning
    the player is in the hashmap, but more than a second has passed till last command, he is
    put again in the hashmap with a new time and the "i" is reset to 0
     * @param event Command preprocess event
     */
    @EventHandler
    private void onPlayerPreCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);

        // if player is in the cooldown hashmap
        if (!commandCooldown.containsKey(uuid)) {
            commandCooldown.put(uuid, System.currentTimeMillis());
        }
        // if the last time of the command is less than a second (1000 milliseconds)
        else if (System.currentTimeMillis() - commandCooldown.get(uuid) < 1000){
            /*
             if "i" is more than the config value number.
             Meaning the player typed a command in the last second more than the allowed count.
            */
            if (i >= CommandConfig.MAX_PER_SEC.getDouble()) {
                player.sendMessage(CommandConfig.COMMAND_SPAM.getText());
                event.setCancelled(true);
            }
            // add i + 1 to increase the amount of times the player has typed a command in the last second
            i++;
        }
        // if the last time player typed a command is more than a second.
        else{
            // put him in the cooldown with the new time of last command used
            commandCooldown.put(uuid, System.currentTimeMillis());
            // reset the integer "i" to default value
            i = 1;
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = EntityCache.getUUID(player);

        commandCooldown.remove(uuid);
    }
}
