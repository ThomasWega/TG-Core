package net.trustgames.core.commands.activity_commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trustgames.core.Core;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Is used to get the logged player's activity by the activity id, rather
 * than the player's name. It always prints just one result in the chat,
 * where player can click on each data, and it will be copied to his clipboard.
 */
public final class ActivityIdCommand extends TrustCommand {

    private final Core core;

    public ActivityIdCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if (core.getMariaDB().isMySQLDisabled()) {
            sender.sendMessage(CommandConfig.COMMAND_DATABASE_OFF.getText());
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ARG.getText().append(
                    Component.text(" Use /activity-id <ID>", NamedTextColor.DARK_GRAY)));
            return;
        }

        String id = args[0];

        // print the data to the chat
        printData(sender, id);
    }

    /**
     * Get the data from the result set and put it in a Component List.
     * Then loop through the list and for each one, send the message to the player.
     *
     * @param sender Command sender
     * @param id     Activity id
     */
    private void printData(CommandSender sender, String id) {
        ActivityFetcher activityQuery = new ActivityFetcher(core);

        // get the result set of the given id
        activityQuery.getActivityByID(id, activity -> {
            try {
                // only one resultSet
                if (activity.next()) {

                    // get all the data from the resultSet
                    String resultId = activityQuery.encodeId(activity.getString("id"));
                    String uuid = activity.getString("uuid");
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    String ip = activity.getString("ip");
                    String action = activity.getString("action");
                    Timestamp time = activity.getTimestamp("time");

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
                sender.sendMessage(CommandConfig.COMMAND_NO_ID_ACT.addID(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
