package net.trustgames.core.player.activity.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.trustgames.core.Core;
import net.trustgames.core.managers.gui.GUIManager;
import net.trustgames.core.managers.gui.InventoryGUI;
import net.trustgames.core.managers.gui.PaginatedGUI;
import net.trustgames.core.managers.gui.buttons.InventoryButton;
import net.trustgames.core.managers.gui.buttons.InventoryPageButton;
import net.trustgames.core.managers.item.ItemBuilder;
import net.trustgames.core.player.activity.config.PlayerActivityMaterials;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.config.CommandConfig;
import net.trustgames.toolkit.config.PermissionConfig;
import net.trustgames.toolkit.database.player.activity.PlayerActivity;
import net.trustgames.toolkit.database.player.activity.PlayerActivityFetcher;
import net.trustgames.toolkit.database.player.data.PlayerDataFetcher;
import net.trustgames.toolkit.managers.HikariManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;

public class ActivityPlayerCommand {

    private final Toolkit toolkit;
    private final HikariManager hikariManager;
    private final PaperCommandManager<CommandSender> commandManager;
    private final GUIManager guiManager;

    public ActivityPlayerCommand(Core core, Command.Builder<CommandSender> activityCommand) {
        this.toolkit = core.getToolkit();
        this.hikariManager = toolkit.getHikariManager();
        this.commandManager = core.getCommandManager();
        this.guiManager = core.getGuiManager();
        register(activityCommand);
    }

    public void register(Command.Builder<CommandSender> activityCommand) {

        // TARGET argument
        CommandArgument<CommandSender, String> targetArg = StringArgument.<CommandSender>builder("target")
                .withSuggestionsProvider((context, s) -> Bukkit.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList())
                .asRequired()
                .build();

        commandManager.command(activityCommand
                .senderType(Player.class)
                .literal("player")
                .permission(PermissionConfig.STAFF.getPermission())
                .argument(targetArg)
                .handler(context -> {
                    Player sender = ((Player) context.getSender());
                    String targetName = context.get(targetArg);

                    handleCommandExecution(sender, targetName);
                })
        );
    }

    private void handleCommandExecution(Player sender,
                                        @NotNull String targetName) {
        createButtons(targetName, optButtons -> {
            if (optButtons.isEmpty()) {
                sender.sendMessage(CommandConfig.COMMAND_NO_PLAYER_DATA.addComponent(Component.text(targetName)));
                return;
            }
            
            PaginatedGUI paginatedGUI = new PaginatedGUI(guiManager, Component.text(targetName + "'s activity"), InventoryGUI.Rows.SIX);
            fillTemplate(paginatedGUI);
            paginatedGUI.paginate(optButtons.get());
            paginatedGUI.openFirstPage(sender);
        });
    }

    private void createButtons(@NotNull String targetName,
                               Consumer<Optional<List<InventoryButton>>> activityItemsCallback) {
        new PlayerDataFetcher(toolkit).resolveUUIDAsync(targetName).thenAccept(optUuid -> {
            if (optUuid.isEmpty()) {
                activityItemsCallback.accept(Optional.empty());
                return;
            }
            PlayerActivityFetcher activityFetcher = new PlayerActivityFetcher(hikariManager);
            activityFetcher.fetchByUUID(optUuid.get()).thenAccept(playerActivity -> {
                if (playerActivity.isEmpty()) {
                    activityItemsCallback.accept(Optional.empty());
                    return;
                }

                List<InventoryButton> buttons = new ArrayList<>();
            /*
             loop through all the results and for each one, set the corresponding
             id, uuid, ip, and time to the lore and set action as the display name.
             Also set the correct Material by using setMaterial method.
             Then add a clone of the ItemStack to the records list
             */
                for (PlayerActivity.Activity activity : playerActivity.get().getActivities()) {
                    long id = activity.getId();
                    String stringUuid = activity.getUuid().toString();
                    String ip = activity.getIp();
                    String action = activity.getAction();
                    Timestamp time = activity.getTime();


                    List<Component> loreList = createLore(time, stringUuid, ip, id);

                    ItemStack activityItem = new ItemBuilder(getMaterial(action), 1)
                            .displayName(Component.empty().append(Component.text(action)).style(Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)))
                            .lore(loreList)
                            .hideFlags()
                            .build();

                    InventoryButton activityButton = new InventoryButton()
                            .creator(player -> activityItem)
                            .consumer(event -> {
                                Player player = ((Player) event.getWhoClicked());
                                player.performCommand("activity id " + id);
                                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            });


                    buttons.add(activityButton);
                }
                activityItemsCallback.accept(Optional.of(buttons));
            });
        });
    }

    private List<Component> createLore(@NotNull Timestamp time,
                                       @NotNull String stringUuid,
                                       @Nullable String ip,
                                       long id) {


        ip = Objects.requireNonNullElse(ip, "UNKNOWN");

        Component dateComp = Component.textOfChildren(Component.text("Date: ").color(NamedTextColor.WHITE))
                .append(Component.textOfChildren(Component.text(time.toLocalDateTime().toLocalDate().toString())).color(NamedTextColor.YELLOW));

        Component timeComp = Component.textOfChildren(Component.text("Time: ").color(NamedTextColor.WHITE))
                .append(Component.textOfChildren(Component.text(time.toLocalDateTime().toLocalTime().toString()).color(NamedTextColor.GOLD)));

        Component uuidComp = Component.textOfChildren(Component.text("UUID: ").color(NamedTextColor.WHITE))
                .append(Component.textOfChildren(Component.text(stringUuid).color(NamedTextColor.GRAY)));

        Component ipComp = Component.textOfChildren(Component.text("IP: ").color(NamedTextColor.WHITE))
                .append(Component.textOfChildren(Component.text(ip)).color(NamedTextColor.GREEN));

        Component idComp = Component.text(id).color(TextColor.fromHexString("#272a2e"));

        Component clickToPrintComp = Component.text("Click to print").color(NamedTextColor.LIGHT_PURPLE);

        return List.of(
                dateComp,
                timeComp,
                Component.empty(),
                uuidComp,
                ipComp,
                Component.empty(),
                idComp,
                clickToPrintComp
        );
    }

    private Material getMaterial(@NotNull String action) {
        for (PlayerActivityMaterials materialEnum : PlayerActivityMaterials.values()) {
            if (action.contains(materialEnum.getActivityType().getAction())) {
                return materialEnum.getIcon();
            }
        }

        // if none actions of the list match, set as BEDROCK
        return Material.BEDROCK;
    }

    private void fillTemplate(PaginatedGUI paginatedGUI) {
        paginatedGUI.setButton(48, new InventoryPageButton()
                .pager(gui -> InventoryPageButton.SwitchAction.PREVIOUS)
                .replace(gui -> new InventoryButton()
                        .creator(player -> new ItemStack(Material.AIR)))
                .creator(player -> new ItemBuilder(Material.ARROW)
                        .displayName(Component.text("Previous page")
                                .color(NamedTextColor.YELLOW))
                        .hideFlags()
                        .build())
                .consumer(event -> paginatedGUI.openPreviousPage(((Player) event.getWhoClicked())))

        );

        paginatedGUI.setButton(50, new InventoryPageButton()
                .pager(gui -> InventoryPageButton.SwitchAction.NEXT)
                .replace(gui -> new InventoryButton()
                        .creator(player -> new ItemStack(Material.AIR)))
                .creator(player -> new ItemBuilder(Material.ARROW)
                        .displayName(Component.text("Next page")
                                .color(NamedTextColor.YELLOW))
                        .hideFlags()
                        .build())
                .consumer(event -> paginatedGUI.openNextPage(((Player) event.getWhoClicked())))

        );

        paginatedGUI.setButton(49, new InventoryButton()
                .creator(player -> new ItemBuilder(Material.KNOWLEDGE_BOOK)
                        .displayName(Component.text("Page " + (paginatedGUI.getPageIndex(player.getUniqueId()) + 1) + "/" + paginatedGUI.getPagesAmount()))
                        .hideFlags()
                        .build()
                )
        );

        int[] fill = new int[]{45, 46, 47, 51, 52, 53};
        Arrays.stream(fill).forEach(value -> paginatedGUI.setButton(value, new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR))));
    }
}
