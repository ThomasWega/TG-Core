package net.trustgames.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import java.util.Objects;

public class CommandManager {

    // used to register all commands
    public static void registerCommand(String commandName, CommandExecutor commandExecutor) {
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand(commandName)).setExecutor(commandExecutor);
    }
}
