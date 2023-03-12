package net.trustgames.core.player.data.commands;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.config.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.PlayerDataConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public final class DataCommand extends TrustCommand {

    private final Core core;
    private final UUIDCache uuidCache;

    public DataCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
        this.uuidCache = core.getUuidCache();
    }

    private PlayerDataType dataType;

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {

        dataType = PlayerDataType.valueOf(label.toUpperCase());

        uuidCache.get(sender.getName(), uuid -> {
            if (args.length == 0) {
                PlayerData playerData = new PlayerData(core, uuid, dataType);
                playerData.getData(data -> sender.sendMessage(PlayerDataConfig.GET_PERSONAL.formatMessage(uuid, dataType, String.valueOf(data))));
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            uuidCache.get(target.getName(), targetUuid -> {
                if (args.length == 1) {
                    PlayerData playerData = new PlayerData(core, targetUuid, dataType);
                    playerData.getData(data -> {
                        if (data == -1) {
                            sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.formatMessage(targetUuid));
                            return;
                        }
                        sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetUuid, dataType, String.valueOf(data)));
                    });
                    return;
                }

                String actionType = args[1];
                int value = 0;
                if (!actionType.equals("get")) {
                    try {
                        value = Integer.parseInt(args[2]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        sender.sendMessage(PlayerDataConfig.INVALID_VALUE.formatMessage(targetUuid, dataType, String.valueOf(value)));
                        return;
                    }
                }

                if (!sender.hasPermission(CorePermissionsConfig.STAFF.permission)) {
                    sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
                    return;
                }

                handleAction(sender, target, actionType, value);
            });
        });
    }

    private void handleAction(CommandSender sender, OfflinePlayer target, String actionType, int value) {
        uuidCache.get(target.getName(), uuid -> {
            PlayerData playerData = new PlayerData(core, uuid, dataType);
            switch (actionType) {
                case "set" -> {
                    playerData.setData(value);
                    sender.sendMessage(PlayerDataConfig.SET.formatMessage(uuid, dataType, String.valueOf(value)));
                }
                case "add" -> {
                    playerData.addData(value);
                    sender.sendMessage(PlayerDataConfig.ADD.formatMessage(uuid, dataType, String.valueOf(value)));
                }
                case "remove" -> {
                    playerData.removeData(value);
                    sender.sendMessage(PlayerDataConfig.REMOVE.formatMessage(uuid, dataType, String.valueOf(value)));
                }
                case "get" -> playerData.getData(data -> {
                    // if there is no data for the player, the value will be "-1"
                    // FIXME ERROR HERE
                    if (data == -1){
                        sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.formatMessage(uuid));
                        return;
                    }
                    sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(uuid, dataType, String.valueOf(data)));
                });
                default -> sender.sendMessage(PlayerDataConfig.INVALID_ACTION
                        .formatMessage(uuid, dataType, String.valueOf(value)));
            }
        });
    }
}
