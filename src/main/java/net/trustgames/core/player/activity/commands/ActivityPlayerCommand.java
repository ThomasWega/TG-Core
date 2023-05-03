package net.trustgames.core.player.activity.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.trustgames.core.Core;
import net.trustgames.core.managers.gui.InventoryGUI;
import net.trustgames.core.managers.gui.PaginatedGUI;
import net.trustgames.core.managers.gui.buttons.InventoryButton;
import net.trustgames.core.managers.gui.buttons.InventoryPageButton;
import net.trustgames.core.managers.item.ItemBuilder;
import net.trustgames.core.player.activity.config.PlayerActivityType;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.cache.UUIDCache;
import net.trustgames.toolkit.config.CommandConfig;
import net.trustgames.toolkit.config.PermissionConfig;
import net.trustgames.toolkit.database.player.activity.PlayerActivity;
import net.trustgames.toolkit.database.player.activity.PlayerActivityFetcher;
import net.trustgames.toolkit.managers.HikariManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;

public class ActivityPlayerCommand extends PaginatedGUI {

    private final Toolkit toolkit;
    private final HikariManager hikariManager;
    private final PaperCommandManager<CommandSender> commandManager;

    public ActivityPlayerCommand(Core core, Command.Builder<CommandSender> activityCommand) {
        super(core.getGuiManager(), Component.text("Player activity"), Rows.SIX);
        this.toolkit = core.getToolkit();
        this.hikariManager = toolkit.getHikariManager();
        this.commandManager = core.getCommandManager();
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
            this.getTemplateGui().setInventoryTitle(Component.text(targetName + "'s activity"));
            this.paginate(optButtons.get());
            this.openFirstPage(sender);
        });
    }

    private void createButtons(@NotNull String targetName,
                               Consumer<Optional<List<InventoryButton>>> activityItems) {
        UUIDCache uuidCache = new UUIDCache(toolkit, targetName);
        uuidCache.get(uuid -> {
            if (uuid.isEmpty()) {
                activityItems.accept(Optional.empty());
                return;
            }
            PlayerActivityFetcher activityFetcher = new PlayerActivityFetcher(hikariManager);
            activityFetcher.fetchByUUID(uuid.get(), playerActivity -> {
                if (playerActivity.isEmpty()) {
                    activityItems.accept(Optional.empty());
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
                            .displayName(Component.text(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + action))
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
                activityItems.accept(Optional.of(buttons));
            });
        });
    }

    private List<Component> createLore(@NotNull Timestamp time,
                                       @NotNull String stringUuid,
                                       @Nullable String ip,
                                       long id) {


        ip = Objects.requireNonNullElse(ip, "UNKNOWN");
        return List.of(
                Component.text(ChatColor.WHITE + "Date: " + ChatColor.YELLOW + time.toLocalDateTime().toLocalDate()),
                Component.text(ChatColor.WHITE + "Time: " + ChatColor.GOLD + time.toLocalDateTime().toLocalTime() + " " +
                        ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)),
                Component.empty(),
                Component.text(ChatColor.WHITE + "UUID: " + ChatColor.GRAY + stringUuid),
                Component.text(ChatColor.WHITE + "IP: " + ChatColor.GREEN + ip),
                Component.empty(),
                Component.text(id).color(TextColor.fromHexString("#272a2e")),
                Component.text(ChatColor.LIGHT_PURPLE + "Click to print")
        );
    }

    private Material getMaterial(@NotNull String action) {
        for (PlayerActivityType activityType : PlayerActivityType.values()) {
            if (action.contains(activityType.getAction())) {
                return activityType.getIcon();
            }
        }

        // if none actions of the list match, set as BEDROCK
        return Material.BEDROCK;
    }

    @Override
    protected InventoryGUI createTemplate() {
        this.setButton(48, new InventoryPageButton()
                .pager(paginatedGUI -> InventoryPageButton.SwitchAction.PREVIOUS)
                .replace(paginatedGUI -> new InventoryButton()
                        .creator(player -> new ItemStack(Material.AIR)))
                .creator(player -> new ItemBuilder(Material.ARROW)
                        .displayName(Component.text("Previous page")
                                .color(NamedTextColor.YELLOW))
                        .hideFlags()
                        .build())
                .consumer(event -> this.openPreviousPage(((Player) event.getWhoClicked())))

        );

        this.setButton(50, new InventoryPageButton()
                .pager(paginatedGUI -> InventoryPageButton.SwitchAction.NEXT)
                .replace(paginatedGUI -> new InventoryButton()
                        .creator(player -> new ItemStack(Material.AIR)))
                .creator(player -> new ItemBuilder(Material.ARROW)
                        .displayName(Component.text("Next page")
                                .color(NamedTextColor.YELLOW))
                        .hideFlags()
                        .build())
                .consumer(event -> this.openNextPage(((Player) event.getWhoClicked())))

        );

        this.setButton(49, new InventoryButton()
                .creator(player -> new ItemBuilder(Material.KNOWLEDGE_BOOK)
                        .displayName(Component.text("Page " + (this.getPageIndex(player.getUniqueId()) + 1) + "/" + this.getPagesAmount()))
                        .hideFlags()
                        .build()
                )
        );

        int[] fill = new int[]{45, 46, 47, 51, 52, 53};
        Arrays.stream(fill).forEach(value -> this.setButton(value, new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR))));

        return this;
    }
}
