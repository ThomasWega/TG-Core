package net.trustgames.core.managers;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles the command cooldowns
 */
public class CommandManager implements Listener {

    private final Core core;

    public CommandManager(Core core) {
        this.core = core;
        core.getServer().getPluginManager().registerEvents(this,core);
    }

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
    public void onPlayerPreCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = core.getConfig();

        // if player is in the cooldown hashmap
        if (!commandCooldown.containsKey(player.getUniqueId())) {
            commandCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }
        // if the last time of the command is less than a second (1000 milliseconds)
        else if (System.currentTimeMillis() - commandCooldown.get(player.getUniqueId()) < 1000){
            /*
             if "i" is more than the config value number.
             Meaning the player typed a command in the last second more than the allowed count.
            */
            if (i >= config.getDouble("cooldowns.max-commands-per-second")) {
                String path = "messages.command-spam";
                player.sendMessage(ColorManager.color(Objects.requireNonNull(
                        config.getString(path), "String on path " + path + " wasn't found in config!")));
                event.setCancelled(true);
            }
            // add i + 1 to increase the amount of times the player has typed a command in the last second
            i++;
        }
        // iff the last time player typed a command is more than a second.
        else{
            // put him in the cooldown with the new time of last command used
            commandCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            // reset the integer "i" to default value
            i = 1;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        commandCooldown.remove(player.getUniqueId());
    }
}
