package net.trustgames.core;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.chat.ChatPrefix;
import net.trustgames.core.chat.MessageLimiter;
import net.trustgames.core.commands.activity_command.ActivityCommand;
import net.trustgames.core.commands.activity_command.ActivityIdCommand;
import net.trustgames.core.commands.messages_commands.MessagesCommand;
import net.trustgames.core.commands.messages_commands.MessagesConfig;
import net.trustgames.core.config.DefaultConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.managers.*;
import net.trustgames.core.playerlist.PlayerListListener;
import net.trustgames.core.playerlist.PlayerListTeams;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;

/**
 * Main class of the Core plugin, which registers all the events and commands.
 * Handles the plugin enable and disable.
 * Has methods to get other instances of other classes and initializes other classes
 * to be able to access them from external plugins
 */
public final class Core extends JavaPlugin {

    final MariaDB mariaDB = new MariaDB(this);
    final ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    final PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    final ServerShutdownManager serverShutdownManager = new ServerShutdownManager(this);
    final GameruleManager gameruleManager = new GameruleManager(this);
    public CooldownManager cooldownManager = new CooldownManager(this);
    Scoreboard playerListScoreboard;
    public LuckPermsManager luckPermsManager;


    // TODO set prefix and cancel chat event conflicts with MessageLimiter class
    // TODO improve config creation and fix unnecessary defaultConfig creation

    @Override
    public void onEnable() {

        /* ADD
        - configurable extendable .yml text info commands (/discord, /store, /help)
        - chat system - add level
        - economy system
        - admin system (vanish, menus, spectate ...)
        - report system
        - level system
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer
        - holo system
        - image maps
        - party and friends system
        - rotating heads
        - npc system
        - disallow some default command (/?, /version, /plugins, etc.)
        - change some default messages (unknown command, etc.)
        - suppress join messages (that should be handled by mini-games core and lobby plugin)
        - chat mention
        - maintenance
        */

        // luckperms
        luckPermsManager = new LuckPermsManager(this);
        luckPermsManager.registerListeners();

        // create DataFolder if not exists
        if(getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        //  FolderManager.createFolder(new File(getDataFolder() + File.separator + "data"));

        // create config files
        ConfigManager.createConfig(new File(getDataFolder(), "announcer.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "mariadb.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "commands.yml"));

        // create config defaults
        DefaultConfig.create(getConfig());
        getConfig().options().copyDefaults(true);
        saveConfig();
        MariaConfig mariaConfig = new MariaConfig(this);
        mariaConfig.createDefaults();
        AnnouncerConfig announcerConfig = new AnnouncerConfig(this);
        announcerConfig.createDefaults();
        MessagesConfig messagesConfig = new MessagesConfig(this);
        messagesConfig.createDefaults();

        // tablist
        PlayerListTeams playerListTeams = new PlayerListTeams(this);
        playerListScoreboard = getServer().getScoreboardManager().getNewScoreboard();
        playerListTeams.createTeams();

        //register events
        registerEvents();

        // register commands
        CommandManager.registerCommand("discord", new MessagesCommand(this));
        CommandManager.registerCommand("website", new MessagesCommand(this));
        CommandManager.registerCommand("store", new MessagesCommand(this));
        CommandManager.registerCommand("activity", new ActivityCommand(this));
        CommandManager.registerCommand("activity-id", new ActivityIdCommand(this));

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();

        // gamerules
        gameruleManager.setGamerules("world");

        // run ChatAnnouncer
        chatAnnouncer.announceMessages();
    }

    @Override
    public void onDisable() {

        // run the server shutdown manager (kick players, write activity, ...)
        serverShutdownManager.kickPlayers();

        // close the HikariCP connection
        mariaDB.closeHikari();
    }

    public MariaDB getMariaDB() {
        return mariaDB;
    }

    private void registerEvents(){
        new ActivityListener(this);
        new CommandManager(this);
        new MessageLimiter(this);
        new CooldownManager(this);
        new ChatPrefix(this);
        new PlayerListListener(this);
        new ActivityCommand(this);
    }


    public static LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    /**
     * Get the player-list scoreboard. The scoreboard needs to be created in
     * the main method, as it needs to be created only once and be same for
     * every player on the server.
     *
     * @return Player-list scoreboard
     */
    public Scoreboard getPlayerListScoreboard() {
        return playerListScoreboard;
    }
}
