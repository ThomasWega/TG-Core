package net.trustgames.core.player.data.commands;

import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.cache.OfflinePlayerCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataConfig;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public final class DataCommand extends TrustCommand {

    private final Core core;

    public DataCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
    }

    private PlayerDataType dataType;

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {

        dataType = PlayerDataType.valueOf(label.toUpperCase());

        UUID uuid = EntityCache.getUUID(Bukkit.getPlayer(sender.getName()));
        if (args.length == 0) {
            PlayerData playerData = new PlayerData(core, uuid, dataType);
            playerData.getData(data -> sender.sendMessage(PlayerDataConfig.GET_PERSONAL.formatMessage(uuid, dataType, String.valueOf(data))));
            return;
        }

        OfflinePlayer target = PlayerUtils.getOfflinePlayer(args[0]);
        UUID targetUUID = OfflinePlayerCache.getUUID(target);
        if (args.length == 1) {
            PlayerData playerData = new PlayerData(core, targetUUID, dataType);
            playerData.getData(data -> {
                if (data == -1) {
                    sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.formatMessage(targetUUID));
                    return;
                }
                sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetUUID, dataType, String.valueOf(data)));
            });
            return;
        }

        String actionType = args[1];
        int value = 0;
        if (!actionType.equals("get")) {
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sender.sendMessage(PlayerDataConfig.INVALID_VALUE.formatMessage(targetUUID, dataType, String.valueOf(value)));
                return;
            }
        }

        if (!sender.hasPermission(CorePermissionsConfig.STAFF.permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return;
        }

        handleAction(sender, target, actionType, value);
    }

    private void handleAction(CommandSender sender, OfflinePlayer target, String actionType, int value) {
        UUID uuid = OfflinePlayerCache.getUUID(target);
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
    }
}
