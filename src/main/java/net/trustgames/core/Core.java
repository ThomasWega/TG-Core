package net.trustgames.core;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import lombok.Getter;
import net.trustgames.core.announcer.AnnounceHandler;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.ChatLimiter;
import net.trustgames.core.chat.commands.TextCommands;
import net.trustgames.core.chat.commands.TextCommandsConfig;
import net.trustgames.core.managers.CommandManager;
import net.trustgames.core.managers.CooldownManager;
import net.trustgames.core.managers.FileManager;
import net.trustgames.core.managers.LuckPermsManager;
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
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
    private final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
    @Getter
    private HikariManager hikariManager;
    @Getter
    private RabbitManager rabbitManager;

    @Override
    public void onEnable() {
        initializeRabbit();
        initializeHikari();
        new AnnounceHandler(this);
        new LuckPermsManager(this);
        new CoreGamerulesHandler();
        TablistTeams.createTeams();

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

        // TODO register commands without plugin.yml -- can set aliases
        // TODO test skin cache (maybe move to redis?)
        // TODO HOLO clickable
        // TODO NPC action - command prints the command in chat
        // TODO NPC protocollib
        // TODO improve player activity (add filters and /activity-ip command)
        // TODO TrustCommand add arguments
        // TODO add tab completion for player-data command
        // TODO activity add ability to check by uuid
        // ADD?: make luckperms async
        // TODO menu/gui/pages manager
        // TODO edit ChatLimiter
        // TODO make CooldownManager per instance!
        // TODO check if I should rather throw from method somewhere rather than catch and throw
        // TODO add the player to database on join
        // TODO add config for rabbitmq

        // FIXME move PlayerDataHandler to proxy
        // FIXME QuitPacket still error -- even when /stop

        // create a data folder
        if (getDataFolder().mkdirs()) {
            getLogger().warning("Created main plugin folder");
        }

        createConfigs();

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        hikariManager.close();
        try {
            rabbitManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerActivityHandler(this), this);
        pluginManager.registerEvents(new CommandManager(), this);
        pluginManager.registerEvents(new CooldownManager(), this);
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
        };

        for (File file : configs) {
            FileManager.createFile(this, file);
        }
    }

    private void initializeHikari(){
        YamlConfiguration mariaConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "mariadb.yml"));
        hikariManager = new HikariManager(
                Objects.requireNonNull(mariaConfig.getString("mariadb.user")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.password")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.ip")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.port")),
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
        rabbitManager = new RabbitManager(
                "guest",
                "guest",
                "localhost",
                5672,
                "proxy",
                false
        );
    }
}
