package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.spawn.SetSpawnCommand;
import net.trustgames.core.spawn.SpawnCommand;
import org.bukkit.command.CommandExecutor;

import java.util.Objects;

public class CommandManager {

    private final Core core;

    public CommandManager(Core core) {
        this.core = core;
    }

    // used to register all commands
    public void registerCommand(String commandName, CommandExecutor commandExecutor) {
        Objects.requireNonNull(core.getCommand(commandName)).setExecutor(commandExecutor);
    }

    // register all commands
    public void registerAllCommands() {
        registerCommand("spawn", new SpawnCommand(core));
        registerCommand("setspawn", new SetSpawnCommand(core));
    }
}
