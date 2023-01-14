package net.trustgames.core.messages_commands;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MessagesCommand implements CommandExecutor {

    private final Core core;

    public MessagesCommand(Core core) {
        this.core = core;
    }

    /*
    there are multiple command in the config file. It is possible to specify custom messages that are sent
    to players for each of the commands. Then, the message is sent, by getting the command name.
    It is not possible to add new commands right in the file, as they need to be in plugin.yml too.
    MAYBE ADD THAT SOON?
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player){
            MessagesConfig messagesConfig = new MessagesConfig(core);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(messagesConfig.getMessagesFile());

            player.sendMessage(ChatColor.translateAlternateColorCodes(('&'), String.join("\n", config.getStringList("messages." + command.getName().toLowerCase()))));

        }
        else{
            Bukkit.getLogger().info(Objects.requireNonNull(core.getConfig().getString("messages.only-in-game-command")));
        }

        return true;
    }
}
