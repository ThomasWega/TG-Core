package net.trustgames.core;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import lombok.Getter;
import net.trustgames.core.announcer.AnnounceHandler;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.ChatLimiter;
import net.trustgames.core.chat.commands.TextCommands;
import net.trustgames.core.chat.commands.TextCommandsConfig;
import net.trustgames.core.managers.CommandManager;
import net.trustgames.core.managers.FileManager;
import net.trustgames.core.managers.gui.GUIListener;
import net.trustgames.core.managers.gui.GUIManager;
import net.trustgames.core.player.PlayerHandler;
import net.trustgames.core.player.activity.PlayerActivityDB;
import net.trustgames.core.player.activity.PlayerActivityHandler;
import net.trustgames.core.player.activity.commands.ActivityCommand;
import net.trustgames.core.player.activity.commands.ActivityIdCommand;
import net.trustgames.core.player.data.PlayerDataDB;
import net.trustgames.core.player.data.PlayerDataHandler;
import net.trustgames.core.player.data.commands.PlayerDataCommand;
import net.trustgames.core.protection.CoreGamerulesHandler;
import net.trustgames.core.tablist.TablistTeams;
import net.trustgames.database.HikariManager;
import net.trustgames.database.RabbitManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main class of the Core plugin, which registers all the events and commands.
 * Handles the plugin enable and disable.
 * Has methods to get other instances of other classes and initializes other classes
 * to be able to access them from external plugins
 */
public final class Core extends JavaPlugin {

    public static final Logger LOGGER = PaperPluginLogger.getLogger("Core");
    @Getter
    @Nullable
    private JedisPool jedisPool = null;
    @Getter
    @Nullable
    private HikariManager hikariManager;
    @Getter
    @Nullable
    private RabbitManager rabbitManager;
    @Getter
    private final GUIManager guiManager = new GUIManager();

    @Override
    public void onEnable() {
        // create a data folder
        if (getDataFolder().mkdirs()) {
            getLogger().warning("Created main plugin folder");
        }

        createConfigs();

        initializeHikari();
        initializeRedis();
        initializeRabbit();
        new AnnounceHandler(this);
        new CoreGamerulesHandler();
        new TablistTeams(this);

        /* ADD
        - chat system - add level
        - economy system
        - admin system (vanish, menus, spectate ...)
        - level system
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer - test skin classes - add redis cache
        - image maps
        - party and friends system
        - rotating heads
        - maintenance
        - playtime bonus
        - boosters
        - autorestart (only if no one is online)
        - menu manager with pagination
        */

        /* SIDE ADDITIONS
        - hover on player name in chat, add info
         */

        /* CHANGE on server side
        - disallow some default command (/?, /version, /plugins, etc.) - make permissions false
        - change some default messages (unknown command, etc.) - change in server .yml files
        - disable pvp on lobbies
         */

        // TODO use aikar acf

        // FIXME just improve npc overall
        // TODO NPC action - command prints the command in chat
        // TODO NPC protocollib

        // TODO register commands without plugin.yml -- can set aliases
        // TODO test skin cache (maybe move to redis?)
        // TODO HOLO clickable
        // TODO improve player activity (add filters and /activity-ip command)
        // TODO TrustCommand add arguments
        // TODO add tab completion for player-data command
        // TODO activity add ability to check by uuid
        // TODO add pagination to GuiManager -- migrate all menus
        // TODO check if I should rather throw from method somewhere rather than catch and throw
        // TODO maybe make some classes interfaces?
        // TODO PlayerDataHandler??

        // FIXME QuitPacket still error -- even when /stop

        // ADD?: make luckperms async
        // ADD?: make CooldownManager per instance!

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (hikariManager != null) {
            hikariManager.close();
        }
        try {
            if (rabbitManager != null) {
                rabbitManager.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new GUIListener(guiManager), this);
        pluginManager.registerEvents(new PlayerActivityHandler(this), this);
        pluginManager.registerEvents(new CommandManager(), this);
        pluginManager.registerEvents(new PlayerHandler(), this);
        pluginManager.registerEvents(new ChatLimiter(), this);
        pluginManager.registerEvents(new ChatDecoration(), this);
        pluginManager.registerEvents(new ActivityCommand(this), this);
        pluginManager.registerEvents(new PlayerDataHandler(this), this);
    }

    private void registerCommands() {
        // List of command to register
        HashMap<PluginCommand, CommandExecutor> cmdList = new HashMap<>();
        cmdList.put(getCommand("activity"), new ActivityCommand(this));
        cmdList.put(getCommand("activity-id"), new ActivityIdCommand(this));
        cmdList.put(getCommand("kills"), new PlayerDataCommand(this));

        // Messages Commands
        for (TextCommandsConfig msgCmd : TextCommandsConfig.values()) {
            cmdList.put(getCommand(msgCmd.name().toLowerCase()), new TextCommands());
        }

        for (PluginCommand cmd : cmdList.keySet()) {
            cmd.setExecutor(cmdList.get(cmd));
        }
    }

    private void createConfigs() {
        File[] configs = new File[]{
                new File(getDataFolder(), "mariadb.yml"),
                new File(getDataFolder(), "rabbitmq.yml"),
                new File(getDataFolder(), "redis.yml")
        };

        FileManager.createFile(this, configs);
    }

    private void initializeHikari(){
        YamlConfiguration mariaConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "mariadb.yml"));
        hikariManager = new HikariManager(
                Objects.requireNonNull(mariaConfig.getString("mariadb.user")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.password")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.ip")),
                String.valueOf(mariaConfig.getInt("mariadb.port")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.database")),
                mariaConfig.getInt("hikaricp.pool-size"),
                !mariaConfig.getBoolean("mariadb.enable")
        );

        hikariManager.onDataSourceInitialized(() -> {
            new PlayerDataDB(hikariManager);
            new PlayerActivityDB(hikariManager);
        });
    }

    private void initializeRabbit(){
        YamlConfiguration rabbitConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rabbitmq.yml"));
        rabbitManager = new RabbitManager(
                Objects.requireNonNull(rabbitConfig.getString("proxy.user")),
                Objects.requireNonNull(rabbitConfig.getString("proxy.password")),
                Objects.requireNonNull(rabbitConfig.getString("proxy.ip")),
                rabbitConfig.getInt("proxy.port"),
                Objects.requireNonNull(rabbitConfig.getString("proxy.queue-name")),
                !rabbitConfig.getBoolean("proxy.enable")
        );
    }

    private void initializeRedis(){
        YamlConfiguration redisConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "redis.yml"));

        if (!redisConfig.getBoolean("redis.enable")) return;

        jedisPool = new JedisPool(
                redisConfig.getString("redis.ip"),
                redisConfig.getInt("redis.port"),
                redisConfig.getString("redis.user"),
                redisConfig.getString("redis.password")
        );
    }
}
