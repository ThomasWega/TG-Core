package net.trustgames.core.commands.messages_commands;

import net.trustgames.core.config.CommandConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Just string or list of strings from a config
 * sent to the player on a given command
 */
public class MessagesCommands implements CommandExecutor {

    /*
    There are multiple commands in the config file (extendable). It is possible to specify custom messages that are sent
    to players for each of the commands. Then, the message is sent, by getting the command name.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player){
            player.sendMessage(MessagesCommandsConfig.valueOf(command.getName().toUpperCase()).getValue());
        }
        else
            Bukkit.getLogger().warning(CommandConfig.COMMAND_ONLY_PLAYER.value.toString());

        return true;
    }
}