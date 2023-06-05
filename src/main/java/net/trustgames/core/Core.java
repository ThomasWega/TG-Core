package net.trustgames.core;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import lombok.Getter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.gui.GUIListener;
import net.trustgames.core.gui.GUIManager;
import net.trustgames.core.player.JoinLeaveMessageDisabler;
import net.trustgames.core.player.data.handler.PlayerDataKillsDeathsHandler;
import net.trustgames.core.player.data.handler.PlayerDataPlaytimeHandler;
import net.trustgames.core.player.display_name.PlayerDisplayNameHandler;
import net.trustgames.core.protection.CoreGamerulesHandler;
import net.trustgames.core.tablist.TablistTeams;
import net.trustgames.core.tablist.TablistTeamsHandler;
import net.trustgames.toolkit.Toolkit;
import net.trustgames.toolkit.database.HikariManager;
import net.trustgames.toolkit.file.FileLoader;
import net.trustgames.toolkit.message_queue.RabbitManager;
import net.trustgames.toolkit.placeholders.PlaceholderManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main class of the Core plugin, which registers all the events and commands.
 * Handles the plugin enable and disable.
 * Has methods to get other instances of other classes and initializes other classes
 * to be able to access them from external plugins
 */
public final class Core extends JavaPlugin {

    public static ComponentLogger LOGGER;
    @Getter
    private final Toolkit toolkit = new Toolkit();
    @Getter
    private GUIManager guiManager;
    @Getter
    private PaperCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        LOGGER = getComponentLogger();

        // create a data folder
        if (getDataFolder().mkdir()) {
            LOGGER.warn("Created main plugin folder {}", getDataFolder().getAbsoluteFile());
        }

        createConfigs();

        initializeHikari();
        initializeRedis();
        initializeRabbit();
        initializePlaceholders();
        guiManager = new GUIManager(this);
        new CoreGamerulesHandler();
        new TablistTeams(this);
        
        /* ADD
        - admin system (vanish, menus, spectate ...)
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer - test skin classes - add redis cache
        - image maps
        - party and friends system
        - maintenance
        - playtime bonus
        - boosters
        - autorestart (only if no one is online)
        - npcs
        */

        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        toolkit.closeConnections();
    }

    private void registerEvents() {
        new GUIListener(this);
        new PlayerDisplayNameHandler(this);
        new ChatDecoration(this);
        new PlayerDataPlaytimeHandler(this);
        new PlayerDataKillsDeathsHandler(this);
        new TablistTeamsHandler(this);
        new JoinLeaveMessageDisabler(this);
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
    }

    private void createConfigs() {
        /*
         1. Name of the file
         2. Folder of the file
        */
        Map<String, File> configsMap = new HashMap<>(Map.of(
                "mariadb.yml", getDataFolder(),
                "rabbitmq.yml", getDataFolder(),
                "redis.yml", getDataFolder()
        ));

        configsMap.forEach((configName, configDir) -> {
            try {
                FileLoader.loadFile(this.getClassLoader(), configDir, configName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config " + configName + "from resources file", e);
            }
        });
    }

    private void initializeHikari() {
        YamlConfiguration mariaConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "mariadb.yml"));
        if (!mariaConfig.getBoolean("mariadb.enable")) {
            LOGGER.warn("HikariCP is disabled");
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

        if (toolkit.getHikariManager() == null) {
            throw new RuntimeException("HikariManager wasn't initialized");
        }

        LOGGER.info("HikariCP is enabled");
    }

    private void initializeRabbit() {
        YamlConfiguration rabbitConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rabbitmq.yml"));
        if (!rabbitConfig.getBoolean("rabbitmq.enable")) {
            LOGGER.warn("RabbitMQ is disabled");
            return;
        }

        toolkit.setRabbitManager(new RabbitManager(
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.user")),
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.password")),
                Objects.requireNonNull(rabbitConfig.getString("rabbitmq.ip")),
                rabbitConfig.getInt("rabbitmq.port"))
        );

        if (toolkit.getRabbitManager() == null) {
            throw new RuntimeException("RabbitManager wasn't initialized");
        }

        LOGGER.info("RabbitMQ is enabled");
    }

    private void initializeRedis() {
        YamlConfiguration redisConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "redis.yml"));
        if (!redisConfig.getBoolean("redis.enable")) {
            LOGGER.warn("Redis is disabled");
            return;
        }

        toolkit.setJedisPool(new JedisPool(
                redisConfig.getString("redis.ip"),
                redisConfig.getInt("redis.port"),
                redisConfig.getString("redis.user"),
                redisConfig.getString("redis.password")
        ));

        if (toolkit.getJedisPool() == null) {
            throw new RuntimeException("JedisPool wasn't initialized");
        }

        LOGGER.info("Redis is enabled");
    }

    private void initializePlaceholders(){
        PlaceholderManager.createPlaceholders(toolkit)
                .filter(Player.class)
                .build()
                .register();
    }
}
