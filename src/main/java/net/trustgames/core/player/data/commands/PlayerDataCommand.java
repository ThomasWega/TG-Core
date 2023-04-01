package net.trustgames.core.player.data.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionConfig;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.config.PlayerDataConfig;
import net.trustgames.core.player.data.config.PlayerDataType;
import net.trustgames.core.utils.ComponentUtils;
import net.trustgames.database.RabbitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Command to get or modify player data in the database
 */
public final class PlayerDataCommand extends TrustCommand {

    private final Core core;
    private PlayerDataType dataType;
    private final RabbitManager rabbitManager;

    public PlayerDataCommand(Core core) {
        super(null);
        this.core = core;
        this.rabbitManager = core.getRabbitManager();
    }

    @Override
    @AllowConsole
    public void execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String senderName = sender.getName();
        dataType = PlayerDataType.valueOf(label.toUpperCase());

        // get the personal players data
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

        // get other player's data
        if (args.length == 1) {
            UUIDCache uuidCache = new UUIDCache(core, targetName);
            uuidCache.get(targetUuid -> {
                if (targetUuid != null) {
                    PlayerDataCache dataCache = new PlayerDataCache(core, targetUuid, dataType);
                    dataCache.get(data -> {
                        if (data != null) {
                            sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(targetName, dataType, data));
                        } else {
                            sender.sendMessage(CommandConfig.COMMAND_NO_PLAYER_DATA.addComponent(Component.text(targetName)));
                        }
                    });
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
        if (!sender.hasPermission(CorePermissionConfig.STAFF.permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return;
        }

        // incorrect usage (action is present, but is missing value)
        if (args.length == 2) {
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ARG.getText().append(
                    Component.text(" Use /" + label + " <Player> [add | remove | set] <value>", NamedTextColor.DARK_GRAY)));
            return;
        }

        // check if the action is valid
        ActionType actionType;
        try {
            actionType = ActionType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e){
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ARG.getText().append(
                    Component.text(" Use /" + label + " <Player> [add | remove | set] <value>", NamedTextColor.DARK_GRAY)));
            return;
        }

        // check if value is an integer
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

    /**
     * Sorts out the correct action (set, remove, add) and
     * modifies the correct data type in the database with the action.
     * Also makes sure the target is a known player and if the target is online,
     * a message is sent to him and the sender
     *
     * @param sender     The sender of the command
     * @param targetName Name of the target
     * @param actionType Type of action to modify the data
     * @param value      Value to modify the data with
     */
    private void handleAction(@NotNull CommandSender sender,
                              @NotNull String targetName,
                              @NotNull ActionType actionType,
                              @NotNull Integer value) {
        UUIDCache uuidCache = new UUIDCache(core, targetName);
        uuidCache.get(targetUuid -> {
            if (targetUuid == null) {
                sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.addComponent(Component.text(targetName)));
                return;
            }

            PlayerData playerData = new PlayerData(core, targetUuid, dataType);
            switch (actionType) {
                case SET -> {
                    playerData.setData(value);
                    sender.sendMessage(PlayerDataConfig.SET_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));

                    // message to target on proxy
                    if (mqOff(sender)) return;
                    JSONObject json = new JSONObject();
                    json.put("player", targetName);
                    json.put("message", ComponentUtils.toJson(PlayerDataConfig.SET_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value))).toString());
                    rabbitManager.send(json);
                }
                case ADD -> {
                    playerData.addData(value);
                    sender.sendMessage(PlayerDataConfig.ADD_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));
                    // message to target on proxy
                    if (mqOff(sender)) return;
                    JSONObject json = new JSONObject();
                    json.put("player", targetName);
                    json.put("message", ComponentUtils.toJson(PlayerDataConfig.ADD_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value))).toString());
                    rabbitManager.send(json);
                }
                case REMOVE -> {
                    playerData.removeData(value);
                    sender.sendMessage(PlayerDataConfig.REMOVE_SENDER.formatMessage(targetName, dataType, String.valueOf(value)));
                    // message to target on proxy
                    if (mqOff(sender)) return;
                    JSONObject json = new JSONObject();
                    json.put("player", targetName);
                    json.put("message", ComponentUtils.toJson(PlayerDataConfig.REMOVE_TARGET.formatMessage(sender.getName(), dataType, String.valueOf(value))).toString());
                    rabbitManager.send(json);
                }
            }
        });
    }

    /**
     * Sends a message to sender when the RabbitMQ is disabled,
     * that a message to target can't be sent
     *
     * @param sender Sender of the command
     * @return true if RabbitMQ is disabled, otherwise false
     */
    private boolean mqOff(CommandSender sender){
        if (rabbitManager.isDisabled()) {
            sender.sendMessage(CommandConfig.COMMAND_MESSAGE_QUEUE_OFF.getText()
                    .append(Component.text("Not sending a message to target player")
                            .color(NamedTextColor.DARK_GRAY)));
            return true;
        } else {
            return false;
        }
    }

    public enum ActionType {
        SET,
        ADD,
        REMOVE
    }
}
