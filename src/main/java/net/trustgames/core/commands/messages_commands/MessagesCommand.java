package net.trustgames.core.commands.messages_commands;

import net.trustgames.core.Core;
import net.trustgames.core.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Just string or list of strings from a config
 * sent to the player on a given command
 */
public class MessagesCommand implements CommandExecutor {

    private final Core core;

    public MessagesCommand(Core core) {
        this.core = core;
    }

    /*
    There are multiple command in the config file (extendable). It is possible to specify custom messages that are sent
    to players for each of the commands. Then, the message is sent, by getting the command name.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player){
            MessagesConfig messagesConfig = new MessagesConfig(core);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(messagesConfig.getMessagesFile());

            player.sendMessage(ColorManager.color(String.join("\n",
                    config.getStringList("messages." + command.getName().toLowerCase()))));

        }
        else{
            String path = "messages.command.only-in-game";
            Bukkit.getLogger().warning(Objects.requireNonNull(core.getConfig().getString(path),
                    "String on path " + path + " wasn't found in config!"));
        }

        return true;
    }
}
