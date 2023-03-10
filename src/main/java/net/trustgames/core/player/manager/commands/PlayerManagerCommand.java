package net.trustgames.core.player.manager.commands;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.PlayerDataConfig;
import net.trustgames.core.player.data.additional.level.PlayerLevel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public final class PlayerManagerCommand extends TrustCommand {

    private final Core core;

    public PlayerManagerCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
    }

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {
        if (args.length < 2) {
            // TODO add config for this message
            // TODO split it up a little
            sender.sendMessage("Usage: /pm <player> <kills|deaths|games|playtime|xp|level|gems|rubies> <set|add|remove|get> <value>");
            return;
        }

        PlayerDataType dataType;
        try{
            dataType = PlayerDataType.valueOf(args[1]);
        }catch (IllegalArgumentException e){
            sender.sendMessage(PlayerDataConfig.INVALID_TYPE
                    // dummy values
                    .formatMessage(UUID.randomUUID(), PlayerDataType.DEATHS, args[1]));
            return;
        }

        String actionType = "get";
        if (args.length > 2){
            actionType = args[2];
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = UUIDCache.get(target.getName());

        int value = 0;
        if (!actionType.equals("get")) {
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sender.sendMessage(PlayerDataConfig.INVALID_VALUE.formatMessage(uuid, PlayerDataType.GEMS, String.valueOf(value))); // gold is dummy type
                return;
            }
        }

        if (dataType == PlayerDataType.LEVEL){
            handlePlayerLevel(sender, target, actionType, value);
        }
        else {
            handlePlayerData(sender, target, dataType, actionType, value);
        }
    }

    private void handlePlayerData(CommandSender sender, OfflinePlayer target, PlayerDataType dataType, String actionType, int value) {
        UUID uuid = UUIDCache.get(target.getName());
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
                if (data == -1){
                    sender.sendMessage(CommandConfig.COMMAND_PLAYER_UNKNOWN.formatMessage(uuid));
                    return;
                }
                sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(uuid, dataType, String.valueOf(value)));
            });
            default -> sender.sendMessage(PlayerDataConfig.INVALID_ACTION
                    .formatMessage(uuid, dataType, String.valueOf(value)));
        }
    }

    private void handlePlayerLevel(CommandSender sender, OfflinePlayer target, String actionType, int value) {
        UUID uuid = UUIDCache.get(target.getName());
        PlayerDataType dataType = PlayerDataType.LEVEL;
        PlayerLevel playerLevel = new PlayerLevel(core, uuid);
        switch (actionType) {
            case "set" -> {
                playerLevel.setLevel(value);
                sender.sendMessage(PlayerDataConfig.SET.formatMessage(uuid, dataType, String.valueOf(value)));
            }
            case "add" -> {
                playerLevel.addLevel(value);
                sender.sendMessage(PlayerDataConfig.ADD.formatMessage(uuid, dataType, String.valueOf(value)));
            }
            case "remove" -> {
                playerLevel.removeLevel(value);
                sender.sendMessage(PlayerDataConfig.REMOVE.formatMessage(uuid, dataType, String.valueOf(value)));
            }
            case "get" -> playerLevel.getLevel(level ->
                    sender.sendMessage(PlayerDataConfig.GET_OTHER.formatMessage(uuid, dataType, String.valueOf(value))));
            default -> sender.sendMessage(PlayerDataConfig.INVALID_ACTION
                    .formatMessage(uuid, dataType, String.valueOf(value)));
        }
    }
}
