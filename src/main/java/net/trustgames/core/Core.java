package net.trustgames.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.announcer.AnnounceHandler;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.ChatLimiter;
import net.trustgames.core.chat.commands.TextCommands;
import net.trustgames.core.chat.commands.TextCommandsConfig;
import net.trustgames.core.managers.*;
import net.trustgames.core.managers.database.DatabaseManager;
import net.trustgames.core.player.activity.PlayerActivityDB;
import net.trustgames.core.player.activity.PlayerActivityHandler;
import net.trustgames.core.player.activity.commands.ActivityCommand;
import net.trustgames.core.player.activity.commands.ActivityIdCommand;
import net.trustgames.core.player.data.PlayerDataDB;
import net.trustgames.core.player.data.commands.DataCommand;
import net.trustgames.core.player.manager.commands.PlayerManagerCommand;
import net.trustgames.core.player.uuid_name.PlayerIDDB;
import net.trustgames.core.player.uuid_name.PlayerIDHandler;
import net.trustgames.core.protection.CoreGamerulesHandler;
import net.trustgames.core.tablist.TablistHandler;
import net.trustgames.core.tablist.TablistTeams;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.HashMap;

/**
 * Main class of the Core plugin, which registers all the events and commands.
 * Handles the plugin enable and disable.
 * Has methods to get other instances of other classes and initializes other classes
 * to be able to access them from external plugins
 */
public final class Core extends JavaPlugin {

    @Getter
    private final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
    @Getter
    private final UUIDCache uuidCache = new UUIDCache(this);
    @Getter
    private final DatabaseManager databaseManager = new DatabaseManager(this);
    public final PlayerDataDB playerDataDB = new PlayerDataDB(this);
    public final PlayerIDDB playerIDDB = new PlayerIDDB(this);
    private final PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    private final AnnounceHandler announceHandler = new AnnounceHandler(this);
    public CooldownManager cooldownManager = new CooldownManager();
    public LuckPermsManager luckPermsManager;

    @Getter
    private final Scoreboard tablistScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    @Getter
    private ProtocolManager protocolManager;

    public static LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    @Override
    public void onEnable() {

        /* ADD
        - chat system - add level
        - economy system
        - admin system (vanish, menus, spectate ...)
        - level system
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer
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

        // TODO register commands without plugin.yml
        // TODO test skin cache
        // TODO test uuid cache
        // TODO HOLO clickable
        // TODO NPC action - command prints the command in chat
        // TODO NPC protocollib
        // TODO TrustCommand add arguments
        // TODO improve player activity (add filters and /activity-ip command)
        // TODO PlayerActivity handler and command have pretty much the same method to fetch by uuid
        // TODO set expiry for the whole hashset of player in redis and add it as configurable value
        // TODO add tab completion for playerdata command
        // TODO playerdata commands add message for the player who got set/added/removed the data
        // TODO playerdata - when player doesn't exist in the database, throws null error
        // TODO also cache level to prevent calculating it everytime - add timer for recalculation or update it on the database column update
        // TODO and also have the level in the database?? NOT SURE
        // TODO try to make a better reformat for player and commands package
        // TODO add config time for uuid cache expiry - will expire on player leave of proxy
        // TODO try to limit the use of uuid cache - same for lobby and proxy plugin
        // TODO move luckperms listeners to different class
        // TODO add comments where missing
        // TODO nameCache add expiry
        // TODO move PlayerIDHandler to proxy
        // TODO figure out if to use the Bukkit.getOffline player or Bukkit.getServer.getOfflinePlayer
        // TODO check command manager if not a bullshit
        // TODO check if uuid is required only later (in if statement or cache for example)
        // TODO add every possible instance of something to core and just make getter
        // TODO maybe change MessageUtils and LuckPerms to Player instead of UUID?
        // TODO merge join listeners which use uuid to one
        // TODO dont format messages by player UUID!!!!


        // FIXME TEST: When restarting, the database connections don't close properly or more are created!
        // FIXME TEST: Is there correct amount of connections?


        // create a data folder
        if (getDataFolder().mkdirs()) {
            getLogger().warning("Created main plugin folder");
        }

        createConfigs();

        // DATABASE
        databaseManager.initializePool();
        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
            /*
            initialize database tables.
            This needs to have a delay, to prevent the DataSource being null
            */
            playerActivityDB.initializeTable();
            playerDataDB.initializeTable();
            playerIDDB.initializeTable();
        }, 20);

        // luckperms
        luckPermsManager = new LuckPermsManager(this);
        luckPermsManager.registerListeners();

        // protocollib
        protocolManager = ProtocolLibrary.getProtocolManager();

        playerList();

        registerEvents();
        registerCommands();

        CoreGamerulesHandler.setGamerules();

        announceHandler.announceMessages();
    }

    @Override
    public void onDisable() {
        databaseManager.closeHikari();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerActivityHandler(this), this);
        pluginManager.registerEvents(new CommandManager(), this);
        pluginManager.registerEvents(new CooldownManager(), this);
        pluginManager.registerEvents(new PlayerManager(), this);
        pluginManager.registerEvents(new ChatLimiter(), this);
        pluginManager.registerEvents(new ChatDecoration(this), this);
        pluginManager.registerEvents(new TablistHandler(this), this);
        pluginManager.registerEvents(new ActivityCommand(this), this);
        pluginManager.registerEvents(new PlayerIDHandler(this), this);
    }

    private void registerCommands() {

        // List of command to register
        HashMap<PluginCommand, CommandExecutor> cmdList = new HashMap<>();
        cmdList.put(getCommand("activity"), new ActivityCommand(this));
        cmdList.put(getCommand("activity-id"), new ActivityIdCommand(this));
        cmdList.put(getCommand("player-manager"), new PlayerManagerCommand(this));
        cmdList.put(getCommand("kills"), new DataCommand(this));

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

        for (File file : configs){
            FileManager.createFile(this, file);
        }
    }

    /**
     * Create the playlist and create teams for it
     * with luckperms groups weight support
     */
    private void playerList() {
       // tablistScoreboard = getServer().getScoreboardManager().getNewScoreboard();
        TablistTeams tablistTeams = new TablistTeams(tablistScoreboard);
        tablistTeams.createTeams();
    }
}
