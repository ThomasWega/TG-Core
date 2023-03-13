package net.trustgames.core.chat.commands;

import net.trustgames.core.config.CommandConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trustgames.core.Core.LOGGER;

/**
 * Just string or list of strings from a config
 * sent to the player on a given command
 */
public final class TextCommands implements CommandExecutor {

    /*
    There are multiple commands in the config file (extendable). It is possible to specify custom messages that are sent
    to players for each of the commands. Then, the message is sent, by getting the command name.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            player.sendMessage(TextCommandsConfig.valueOf(command.getName().toUpperCase()).getValue());
        } else
            LOGGER.warning(CommandConfig.COMMAND_PLAYER_ONLY.value.toString());

        return true;
    }
}
