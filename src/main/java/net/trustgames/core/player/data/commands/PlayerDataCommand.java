package net.trustgames.core.player.data.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.PlayerDataConfig;
import net.trustgames.core.player.data.config.PlayerDataType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlayerDataCommand extends TrustCommand {

    private final Core core;
    private PlayerDataType dataType;

    public PlayerDataCommand(Core core) {
        super(CorePermissionsConfig.DEFAULT.permission);
        this.core = core;
    }

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {
        String senderName = sender.getName();
        dataType = PlayerDataType.valueOf(label.toUpperCase());

        if (args.length == 0) {
            UUIDCache uuidCache = new UUIDCache(core, senderName);
            uuidCache.get(uuid -> {
                /*
                 will most likely mean that console executed this command
                 and console can't have uuid
                */
                if (uuid == null) {
                    sender.sendMessage(CommandConfig.COMMAND_PLAYER_ONLY.getText());
                    return;
                }
                PlayerDataCache dataCache = new PlayerDataCache(core, uuid, dataType);
                dataCache.get(data -> sender.sendMessage(PlayerDataConfig.GET_PERSONAL.formatMessage(senderName, dataType, String.valueOf(data))));
            });
            return;
        }


        String targetName = args[0];

        if (args.length == 1) {
            UUIDCache uuidCache = new UUIDCache(core, targetName);
            uuidCache.get(targetUuid -> {
                if (targetUuid != null) {
                    PlayerDataCache dataCache = new PlayerDataCache(core, targetUuid, dataType);
                    dataCache.get(data -> sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetName, dataType, data)));
                } else {
                    sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.addComponent(Component.text(targetName)));
                }
            });
            return;
        }

        /*
        - STAFF SECTION -
        The below code is only for staff, as it includes
        setting, adding, or removing a player data
         */
        if (!sender.hasPermission(CorePermissionsConfig.STAFF.permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return;
        }

        if (args.length == 2){
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ARG.getText().append(
                    Component.text(" Use /" + label + " <Player> [add | remove | set] <value>", NamedTextColor.DARK_GRAY)));
            return;
        }

        String actionType = args[1];
        String stringValue = args[2];
        int value;
        try {
            value = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            sender.sendMessage(CommandConfig.COMMAND_INVALID_VALUE.addComponent(Component.text(stringValue)));
            return;
        }

        handleAction(sender, targetName, actionType, value);
    }

    private void handleAction(CommandSender sender, String targetName, String actionType, int value) {
        UUIDCache uuidCache = new UUIDCache(core, targetName);
        uuidCache.get(targetUuid -> {
            if (targetUuid == null) {
                sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.addComponent(Component.text(targetName)));
                return;
            }

            boolean targetMessage = false;
            Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                targetMessage = true;
            }

            PlayerData playerData = new PlayerData(core, targetUuid, dataType);
            switch (actionType) {
                case "set" -> {
                    playerData.setData(value);
                    sender.sendMessage(PlayerDataConfig.SET_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));
                    if (targetMessage)
                        target.sendMessage(PlayerDataConfig.SET_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value)));
                }
                case "add" -> {
                    playerData.addData(value);
                    sender.sendMessage(PlayerDataConfig.ADD_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));
                    if (targetMessage)
                        target.sendMessage(PlayerDataConfig.ADD_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value)));
                }
                case "remove" -> {
                    playerData.removeData(value);
                    sender.sendMessage(PlayerDataConfig.REMOVE_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));
                    if (targetMessage)
                        target.sendMessage(PlayerDataConfig.REMOVE_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value)));
                }
                default -> sender.sendMessage(PlayerDataConfig.INVALID_ACTION
                        .formatMessage(targetName, dataType, String.valueOf(value)));
            }
        });
    }
}
