package net.trustgames.core.player.activity.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.paper.PaperCommandManager;
import net.trustgames.core.Core;
import org.bukkit.command.CommandSender;

public class ActivityCommands {

    private final Core core;
    private final PaperCommandManager<CommandSender> commandManager;

    public ActivityCommands(Core core) {
        this.core = core;
        this.commandManager = core.getCommandManager();
        registerCommands();
    }

    private void registerCommands(){
        // MAIN SHARED COMMAND
        Command.Builder<CommandSender> activityCommand = commandManager.commandBuilder("activity",
                ArgumentDescription.of("ADD")
        );

        new ActivityIdCommand(core, activityCommand);
        new ActivityPlayerCommand(core, activityCommand);
    }
}
