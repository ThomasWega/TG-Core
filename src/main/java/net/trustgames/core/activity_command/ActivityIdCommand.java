package net.trustgames.core.activity_command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.trustgames.core.Core;
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

public class ActivityIdCommand implements CommandExecutor {

    private final Core core;

    public ActivityIdCommand(Core core) {
        this.core = core;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = core.getConfig();

        if (sender.hasPermission("core.staff")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.command-invalid-argument") + "&8 Use /activity-id <id>"));
                return true;
            }

            String id = args[0];
            printData(sender, id);
        }
        return true;
    }

    public void printData(CommandSender sender, String id){
        FileConfiguration config = core.getConfig();

        ActivityQuery activityQuery = new ActivityQuery(core);
        ResultSet resultSet = activityQuery.getActivityByID(id);

        try {
            if (resultSet.next()) {
                String resultId = activityQuery.encodeId(resultSet.getString("id"));
                String uuid = resultSet.getString("uuid");
                String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                String ip = resultSet.getString("ip");
                String action = resultSet.getString("action");
                Timestamp time = resultSet.getTimestamp("time");

                if (name == null) {
                    name = "ERROR: No data";
                }

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

                for (Component s : chatMessage) {
                    sender.sendMessage(s);
                }
                return;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(Objects.requireNonNull(config.getString("messages.command-no-id-activity")), id)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
