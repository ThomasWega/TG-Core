package net.trustgames.core.player.data.manager;

import net.trustgames.core.Core;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.player.data.level.PlayerLevel;
import net.trustgames.core.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PlayerDataCommand extends TrustCommand {

    private final Core core;

    public PlayerDataCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
    }

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args) {
        try {
            OfflinePlayer target = PlayerUtils.getOfflinePlayer(args[0]);
            String statsType = args[1];
            switch (statsType) {
                case "level":
                    if (args.length < 4) {
                        sender.sendMessage("/pm <player> level set <amount>");
                        sender.sendMessage("/pm <player> level add <amount>");
                        sender.sendMessage("/pm <player> level remove <amount>");
                        return;
                    }
                    String actionType = args[2];
                    int value;
                    try{
                        value = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e){
                        sender.sendMessage("Invalid value!");
                        return;
                    }
                    PlayerLevel playerLevel = new PlayerLevel(core, target.getUniqueId());
                    switch (actionType){
                        case "set":
                            playerLevel.setLevel(value);
                            sender.sendMessage("set");
                            return;
                        case "add":
                            playerLevel.addLevel(value);
                            sender.sendMessage("add");
                            return;
                        case "remove":
                            playerLevel.removeLevel(value);
                            sender.sendMessage("remove");
                            return;
                    }
                    break;
                case "coins":
                    if (args.length < 3) {
                        sender.sendMessage("/pm <player> coins set <amount>");
                        sender.sendMessage("/pm <player> coins add <amount>");
                        sender.sendMessage("/pm <player> coins remove <amount>");
                        return;
                    }
                    // Handle coin actions with args[3]
                    break;
                // Handle other stats types
            }
        } catch(ArrayIndexOutOfBoundsException e){
            sender.sendMessage("Usage: /pm <player> <coins|gems|level|xp|kills|deaths> <add|remove|set> <amount>");
        }
    }
}
