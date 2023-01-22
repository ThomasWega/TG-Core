package net.trustgames.core.commands.activity_command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.trustgames.core.Core;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Is used to get the logged player's activity by the activity id, rather
 * than the player's name. It always prints just one result in the chat,
 * where player can click on each data, and it will be copied to his clipboard.
 */
public class ActivityIdCommand implements CommandExecutor {

    private final Core core;

    public ActivityIdCommand(Core core) {
        this.core = core;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender.hasPermission("core.staff")) {
            
            if (core.getMariaDB().isMySQLDisabled()){
                String path = "messages.mariadb.disabled";
                sender.sendMessage(ColorUtils.colorString(Objects.requireNonNull(
                        config.getString(path), "String on path " + path + " wasn't found in config!")));
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ColorUtils.colorString(
                        config.getString("messages.command.invalid-argument") + "&8 Use /activity-id <id>"));
                return true;
            }

            String id = args[0];

            // print the data to the chat
            printData(sender, id);
        }
        return true;
    }

    /**
     * Get the data from the result set and put it in a Component List.
     * Then loop through the list and for each one, send the message to the player.
     *
     * @param sender Command sender
     * @param id Activity id
     */
    private void printData(CommandSender sender, String id){
        FileConfiguration config = core.getConfig();

        ActivityQuery activityQuery = new ActivityQuery(core);

        // get the result set of the given id
        ResultSet resultSet = activityQuery.getActivityByID(id);

        try {
            // only one resultSet
            if (resultSet.next()) {

                // get all the data from the resultSet
                String resultId = activityQuery.encodeId(resultSet.getString("id"));
                String uuid = resultSet.getString("uuid");
                String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                String ip = resultSet.getString("ip");
                String action = resultSet.getString("action");
                Timestamp time = resultSet.getTimestamp("time");

                if (name == null) {
                    name = "ERROR: No data";
                }

                // list of component messages
                List<Component> chatMessage = new ArrayList<>();
                chatMessage.add(Component.text(ChatColor.DARK_GRAY + "------------------------"));
                chatMessage.add(Component.text(ChatColor.WHITE + "Name: " + ChatColor.RED + name).clickEvent(ClickEvent.copyToClipboard(name)));
                chatMessage.add(Component.text(ChatColor.WHITE + "IP: " + ChatColor.YELLOW + ip).clickEvent(ClickEvent.copyToClipboard(ip)));
                chatMessage.add(Component.text(ChatColor.WHITE + "UUID: " + ChatColor.GRAY + uuid).clickEvent(ClickEvent.copyToClipboard(uuid)));
                chatMessage.add(Component.text(""));
                chatMessage.add(Component.text(ChatColor.WHITE + "Action: " + ChatColor.GOLD + action).clickEvent(ClickEvent.copyToClipboard(action)));
                chatMessage.add(Component.text(ChatColor.WHITE + "Date: " + ChatColor.GREEN + time.toLocalDateTime().toLocalDate()).clickEvent(ClickEvent.copyToClipboard(time.toLocalDateTime().toLocalDate().toString())));
                chatMessage.add(Component.text(ChatColor.WHITE + "Time: " + ChatColor.DARK_GREEN + time.toLocalDateTime().toLocalTime() + " " + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)).clickEvent(ClickEvent.copyToClipboard(time.toLocalDateTime().toLocalTime() + " " + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT))));
                chatMessage.add(Component.text(""));
                chatMessage.add(Component.text(ChatColor.WHITE + "ID: " + ChatColor.DARK_PURPLE + resultId).clickEvent(ClickEvent.copyToClipboard(resultId)));
                chatMessage.add(Component.text(ChatColor.DARK_GRAY + "------------------------"));

                // loop through the list and for each, send a message
                for (Component s : chatMessage) {
                    sender.sendMessage(s);
                }
                return;
            }
            String path = "messages.command.no-id-activity";
            sender.sendMessage(ColorUtils.colorString(String.format(Objects.requireNonNull(
                    config.getString(path), "String on path " + path + " wasn't found in config!"), id)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
