package net.trustgames.core.player.activity.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.Core;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.config.CommandConfig;
import net.trustgames.toolkit.config.PermissionConfig;
import net.trustgames.toolkit.database.player.activity.PlayerActivity;
import net.trustgames.toolkit.database.player.activity.PlayerActivityFetcher;
import net.trustgames.toolkit.database.player.activity.config.PlayerAction;
import net.trustgames.toolkit.database.player.data.PlayerDataFetcher;
import net.trustgames.toolkit.database.player.data.config.PlayerDataType;
import net.trustgames.toolkit.database.HikariManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Is used to get the logged player's activity by the activity id, rather
 * than the player's name. It always prints just one result in the chat,
 * where player can click on each data, and it will be copied to his clipboard.
 */
public final class ActivityIdCommand {

    private final PaperCommandManager<CommandSender> commandManager;
    private final Toolkit toolkit;
    private final HikariManager hikariManager;


    public ActivityIdCommand(Core core, Command.Builder<CommandSender> activityCommand) {
        this.commandManager = core.getCommandManager();
        this.toolkit = core.getToolkit();
        this.hikariManager = toolkit.getHikariManager();
        register(activityCommand);
    }


    private void register(Command.Builder<CommandSender> activityCommand) {

        // VALUE argument
        CommandArgument<CommandSender, Long> idArg = LongArgument.<CommandSender>builder("id")
                .withMin(0L)
                .build();

        commandManager.command(activityCommand
                .literal("id", ArgumentDescription.of("ADD"))
                .permission(PermissionConfig.STAFF.getPermission())
                .argument(idArg)
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    long id = context.get("id");
                    printData(sender, id);
                })
        );
    }

    /**
     * Get the data from the result set and put it in a Component List.
     * Then loop through the list and for each one, send the message to the player.
     *
     * @param sender Command sender
     * @param id     Activity id
     */
    private void printData(@NotNull CommandSender sender, long id) {
        PlayerActivityFetcher activityFetcher = new PlayerActivityFetcher(hikariManager);
        activityFetcher.fetchByID(id).thenAccept(optActivity -> {
            if (optActivity.isEmpty()) {
                sender.sendMessage(CommandConfig.COMMAND_NO_ID_DATA.addComponent(Component.text(id)));
                return;
            }

            PlayerActivity.Activity activity = optActivity.get();

            // get all the data from the resultSet
            long resultId = activity.getId();
            UUID uuid = activity.getUuid();
            String ip = activity.getIp();
            PlayerAction action = activity.getAction();
            Timestamp time = activity.getTime();

            if (ip == null)
                ip = "UNABLE TO FETCH";

            String finalIp = ip;
            new PlayerDataFetcher(toolkit).resolveDataAsync(uuid, PlayerDataType.NAME).thenAccept(optName -> {
                String name = optName.map(Object::toString).orElse("UNABLE TO FETCH");

                // loop through the list and for each, send a message
                for (Component s : createMessage(uuid, name, finalIp, action, time, resultId)) {
                    sender.sendMessage(s);
                }
            });
        });
    }

    // Just creates table with the fetched data and saves it as List of Components
    private List<Component> createMessage(@NotNull UUID uuid,
                                          @NotNull String name,
                                          @NotNull String finalIp,
                                          @NotNull PlayerAction action,
                                          @NotNull Timestamp time,
                                          long id) {
        Component nameComp = Component.text("Name: ", NamedTextColor.WHITE)
                .append(Component.text(name, TextColor.fromHexString("#FF5555")))
                .clickEvent(ClickEvent.copyToClipboard(name));

        Component ipComp = Component.text("IP: ", NamedTextColor.WHITE)
                .append(Component.text(finalIp, TextColor.fromHexString("#FFCC33")))
                .clickEvent(ClickEvent.copyToClipboard(finalIp));

        Component uuidComp = Component.text("UUID: ", NamedTextColor.WHITE)
                .append(Component.text(uuid.toString(), TextColor.fromHexString("#A0A0A0")))
                .clickEvent(ClickEvent.copyToClipboard(uuid.toString()));

        Component actionComp = Component.text("Action: ", NamedTextColor.WHITE)
                .append(Component.text(action.getActionString(), TextColor.fromHexString("#0dc9de")))
                .clickEvent(ClickEvent.copyToClipboard(action.getActionString()));

        Component dateTimeComp = Component.text("Date/Time: ", NamedTextColor.WHITE)
                .append(Component.text(time.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), TextColor.fromHexString("#07e015")))
                .clickEvent(ClickEvent.copyToClipboard(time.toLocalDateTime().toString()));

        Component idComp = Component.text("ID: ", NamedTextColor.WHITE)
                .append(Component.text(String.valueOf(id), TextColor.fromHexString("#ed1186")))
                .clickEvent(ClickEvent.copyToClipboard(String.valueOf(id)));


        return List.of(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<bold><color:#3e403e>--------------------"),
                nameComp,
                ipComp,
                uuidComp,

                Component.empty(),

                actionComp,
                dateTimeComp,

                Component.empty(),

                idComp,
                MiniMessage.miniMessage().deserialize("<bold><color:#3e403e>--------------------"),
                Component.empty()
        );
    }
}
