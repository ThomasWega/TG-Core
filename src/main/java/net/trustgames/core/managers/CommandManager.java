package net.trustgames.core.managers;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CommandManager implements Listener {

    private final Core core;

    public CommandManager(Core core) {
        this.core = core;
    }

    HashMap<UUID, Long> commandCooldown = new HashMap<>();

    // used to register all commands
    public static void registerCommand(String commandName, CommandExecutor commandExecutor) {
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand(commandName)).setExecutor(commandExecutor);
    }

    /* limit the number of commands player can send, to avoid spamming.
    it checks, if the player is already in the hashmap,
    or if the current time - the time in hashmap is larger than the delay
    in seconds * 1000 (to convert to milliseconds) specified in config,
    and if the player doesn't have the staff permission
     */
    @EventHandler
    public void onPlayerPreCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = core.getConfig();
        System.out.println("1");
        if (player.hasPermission(Objects.requireNonNull(config.getString("permissions.staff")))) return;
        System.out.println("2");
        if (!commandCooldown.containsKey(player.getUniqueId()) || System.currentTimeMillis() - commandCooldown.get(player.getUniqueId()) > (config.getDouble("settings.max-commands-in-second") * 1000)) {
            commandCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", Objects.requireNonNull(config.getString("messages.command-spam")))));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        commandCooldown.remove(player.getUniqueId());
    }
}
