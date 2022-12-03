package net.trustgames.core.spawn;

import net.trustgames.core.Core;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record SpawnCommand(Core core) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            Spawn spawn = new Spawn(core);

            // gets the location from the spawn.yml
            YamlConfiguration config = YamlConfiguration.loadConfiguration(spawn.getSpawnFile());
            Location location = config.getLocation("spawn.location");
            if (location != null) {
                player.teleport(location);
            } else {
                player.sendMessage(ChatColor.RED + "Spawn location isn't set!");
            }
        }
        else{
            sender.sendMessage(ChatColor.RED + "This command can be used by players only!");
        }
        return true;
    }
}
