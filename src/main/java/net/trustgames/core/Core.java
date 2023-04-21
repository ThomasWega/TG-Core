package net.trustgames.core;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.destroystokyo.paper.utils.PaperPluginLogger;
import lombok.Getter;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.managers.CommandManager;
import net.trustgames.core.managers.FileManager;
import net.trustgames.core.managers.gui.GUIListener;
import net.trustgames.core.managers.gui.GUIManager;
import net.trustgames.core.player.PlayerHandler;
import net.trustgames.core.player.activity.PlayerActivityHandler;
import net.trustgames.core.player.activity.commands.ActivityIdCommand;
import net.trustgames.core.player.activity.commands.ActivityPlayerCommand;
import net.trustgames.core.protection.CoreGamerulesHandler;
import net.trustgames.core.tablist.TablistHandler;
import net.trustgames.core.tablist.TablistTeams;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.database.player.activity.PlayerActivityDB;
import net.trustgames.toolkit.database.player.data.PlayerDataDB;
import net.trustgames.toolkit.managers.HikariManager;
import net.trustgames.toolkit.managers.rabbit.RabbitManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

import java.io.File;
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
    private final Toolkit toolkit = new Toolkit();

    @Getter
    private final GUIManager guiManager = new GUIManager();

    @Getter
    private PaperCommandManager<CommandSender> commandManager;

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

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        HikariManager hikariManager = toolkit.getHikariManager();
        JedisPool jedisPool = toolkit.getJedisPool();
        RabbitManager rabbitManager = toolkit.getRabbitManager();

        if (hikariManager != null) {
            hikariManager.close();
        }
        if (jedisPool != null){
            jedisPool.close();
        }
        if (rabbitManager != null) {
            rabbitManager.close();
        }
    }

    private void registerEvents() {
        final PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new GUIListener(guiManager), this);
        pluginManager.registerEvents(new PlayerActivityHandler(this), this);
        pluginManager.registerEvents(new CommandManager(), this);
        pluginManager.registerEvents(new PlayerHandler(), this);
        pluginManager.registerEvents(new TablistHandler(), this);
        pluginManager.registerEvents(new ChatDecoration(), this);
        pluginManager.registerEvents(new ActivityPlayerCommand(this), this);
    }

    private void registerCommands() {
        try {
            commandManager = PaperCommandManager.createNative(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Command Manager", e);
        }

        new ActivityPlayerCommand(this);
        new ActivityIdCommand(this);
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
        if (!mariaConfig.getBoolean("mariadb.enable")) {
            LOGGER.warning("HikariCP is disabled");
            return;
        }

        toolkit.setHikariManager(new HikariManager(
                Objects.requireNonNull(mariaConfig.getString("mariadb.user")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.password")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.ip")),
                String.valueOf(mariaConfig.getInt("mariadb.port")),
                Objects.requireNonNull(mariaConfig.getString("mariadb.database")),
                mariaConfig.getInt("hikaricp.pool-size")
                ));

        HikariManager hikariManager = toolkit.getHikariManager();
        if (hikariManager == null) return;
        hikariManager.onDataSourceInitialized(() -> {
            new PlayerDataDB(hikariManager);
            new PlayerActivityDB(hikariManager);
        });

        LOGGER.info("HikariCP is enabled");
    }

    private void initializeRabbit(){
        YamlConfiguration rabbitConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rabbitmq.yml"));
        if (!rabbitConfig.getBoolean("rabbitmq.enable")) {
            LOGGER.warning("RabbitMQ is disabled");
            return;
        }

        toolkit.setRabbitManager(new RabbitManager(
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.user")),
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.password")),
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.ip")),
                rabbitConfig.getInt("rabbitmq.port"))
        );

        LOGGER.info("RabbitMQ is enabled");
    }

    private void initializeRedis(){
        YamlConfiguration redisConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "redis.yml"));
        if (!redisConfig.getBoolean("redis.enable")) {
            LOGGER.warning("Redis is disabled");
            return;
        }

        toolkit.setJedisPool(new JedisPool(
                redisConfig.getString("redis.ip"),
                redisConfig.getInt("redis.port"),
                redisConfig.getString("redis.user"),
                redisConfig.getString("redis.password")
        ));

        LOGGER.info("Redis is enabled");
    }
}
