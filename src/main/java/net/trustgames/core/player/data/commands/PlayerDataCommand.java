package net.trustgames.core.player.data.commands;

import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.PlayerDataConfig;
import org.bukkit.command.CommandSender;

public final class PlayerDataCommand extends TrustCommand {

    private final Core core;

    public PlayerDataCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
    }

    private PlayerDataType dataType;

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {
        String senderName = sender.getName();
        dataType = PlayerDataType.valueOf(label.toUpperCase());

        if (args.length == 0) {
            UUIDCache uuidCache = new UUIDCache(core, senderName);
            uuidCache.get(uuid -> {
                PlayerDataCache dataCache = new PlayerDataCache(core, uuid, dataType);
                dataCache.get(data -> sender.sendMessage(PlayerDataConfig.GET_PERSONAL.formatMessage(senderName, dataType, String.valueOf(data))));
            });
            return;
        }


        String targetName = args[0];

        if (args.length == 1) {
            UUIDCache uuidCache = new UUIDCache(core, targetName);
            uuidCache.get(targetUuid -> {
                PlayerDataCache dataCache = new PlayerDataCache(core, targetUuid, dataType);
                dataCache.get(data -> {
                    if (data == null) {
                        sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.addName(targetName));
                        return;
                    }
                    sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetName, dataType, data));
                });
            });
            return;
        }

        String actionType = args[1];
        int value = 0;
        if (!actionType.equals("get")) {
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sender.sendMessage(PlayerDataConfig.INVALID_VALUE.formatMessage(targetName, dataType, String.valueOf(value)));
                return;
            }
        }

        if (!sender.hasPermission(CorePermissionsConfig.STAFF.permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return;
        }

        handleAction(sender, targetName, actionType, value);
    }

    private void handleAction(CommandSender sender, String targetName, String actionType, int value) {
        UUIDCache uuidCache = new UUIDCache(core, targetName);
        uuidCache.get(targetUuid -> {
            PlayerData playerData = new PlayerData(core, targetUuid, dataType);
            switch (actionType) {
                case "set" -> {
                    playerData.setData(value);
                    sender.sendMessage(PlayerDataConfig.SET.formatMessage(targetName, dataType, String.valueOf(value)));
                }
                case "add" -> {
                    playerData.addData(value);
                    sender.sendMessage(PlayerDataConfig.ADD.formatMessage(targetName, dataType, String.valueOf(value)));
                }
                case "remove" -> {
                    playerData.removeData(value);
                    sender.sendMessage(PlayerDataConfig.REMOVE.formatMessage(targetName, dataType, String.valueOf(value)));
                }
                case "get" -> {
                    PlayerDataCache dataCache = new PlayerDataCache(core, targetUuid, dataType);
                    dataCache.get(data -> {
                        int intData = Integer.parseInt(data);
                        if (intData == -1) {
                            sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.addName(targetName));
                            return;
                        }
                        sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetName, dataType, data));
                    });
                }
                default -> sender.sendMessage(PlayerDataConfig.INVALID_ACTION
                        .formatMessage(targetName, dataType, String.valueOf(value)));
            }
        });
    }
}
