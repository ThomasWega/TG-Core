package net.trustgames.core;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import lombok.Getter;
import net.trustgames.core.announcer.AnnounceHandler;
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
import net.trustgames.core.player.data.PlayerDataHandler;
import net.trustgames.core.player.data.commands.PlayerDataCommand;
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
    private final DatabaseManager databaseManager = new DatabaseManager(this);
    @Getter
    private final Scoreboard tablistScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    @Override
    public void onEnable() {
        final PlayerDataDB playerDataDB = new PlayerDataDB(this);
        final PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
        final AnnounceHandler announceHandler = new AnnounceHandler(this);
        final LuckPermsManager luckPermsManager = new LuckPermsManager(this);

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

        // TODO register commands without plugin.yml -- can set aliases
        // TODO test skin cache (maybe move to redis?)
        // TODO HOLO clickable
        // TODO NPC action - command prints the command in chat
        // TODO NPC protocollib
        // TODO TrustCommand add arguments
        // TODO improve player activity (add filters and /activity-ip command)
        // TODO PlayerActivity handler and command have pretty much the same method to fetch by uuid
        // TODO add tab completion for playerdata command
        // TODO playerdata commands add message for the player who got set/added/removed the data
        // TODO also cache level to prevent calculating it everytime - add timer for recalculation or update it on the database column update
        // TODO add comments where missing
        // TODO move PlayerDataHandler to proxy
        // TODO figure out if to use the Bukkit.getOffline player or Bukkit.getServer.getOfflinePlayer
        // TODO check command manager if not a bullshit
        // TODO merge join listeners which use uuid to one
        // TODO annotate all things correctly
        // TODO move data commands to proxy ?? MAYBE
        // TODO move tablist to proxy
        // TODO check if things can be taken in constructor instead of methods
        // TODO dont allow to set any data if player never joined!
        // TODO create an event when a data in database updates
        // TODO activity add ability to check by uuid
        // TODO make luckperms async

        // FIXME scoreboard null on first start
        // FIXME datasource null on first/second time
        // FIXME @AllowConsole doesn't work


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
        }, 20);

        // luckperms
        luckPermsManager.registerListeners();

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
        pluginManager.registerEvents(new ChatDecoration(), this);
        pluginManager.registerEvents(new TablistHandler(this), this);
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

        for (File file : configs){
            FileManager.createFile(this, file);
        }
    }

    /**
     * Create the playlist and create teams for it
     * with luckperms groups weight support
     */
    private void playerList() {
        TablistTeams tablistTeams = new TablistTeams(tablistScoreboard);
        tablistTeams.createTeams();
    }
}
