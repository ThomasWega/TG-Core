package net.trustgames.core;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.chat.ChatPrefix;
import net.trustgames.core.chat.MessageLimiter;
import net.trustgames.core.commands.MessagesCommand;
import net.trustgames.core.commands.MessagesConfig;
import net.trustgames.core.config.DefaultConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.managers.*;
import net.trustgames.core.player_activity.ActivityCommand;
import net.trustgames.core.playerlist.PlayerListListener;
import net.trustgames.core.playerlist.PlayerListTeams;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;

public final class Core extends JavaPlugin {
    MariaDB mariaDB = new MariaDB(this);
    ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    ServerShutdownManager serverShutdownManager = new ServerShutdownManager(this);
    GameruleManager gameruleManager = new GameruleManager(this);
    public CooldownManager cooldownManager = new CooldownManager(this);
    Scoreboard playerListScoreboard;
    public LuckPermsManager luckPermsManager;



    @Override
    public void onEnable() {

        /* ADD
        - Command completer (tab complete)
        - configurable extendable .yml text info commands (/discord, /store, /help)
        - chat system
        - economy system
        - admin system (vanish, menus, ...)
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
        */

        // luckperms
        luckPermsManager = new LuckPermsManager(this);
        luckPermsManager.registerListeners();

        // create a folder
        FolderManager.createDataFolder(getDataFolder());
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

        // register events
        EventManager.registerEvent(new ActivityListener(this), this);
        EventManager.registerEvent(new CommandManager(this), this);
        EventManager.registerEvent(new MessageLimiter(this), this);
        EventManager.registerEvent(new CooldownManager(this), this);
        EventManager.registerEvent(new ChatPrefix(this), this);
        EventManager.registerEvent(new PlayerListListener(this), this);
        EventManager.registerEvent(new ActivityCommand(this), this);

        // register commands
        CommandManager.registerCommand("discord", new MessagesCommand(this));
        CommandManager.registerCommand("website", new MessagesCommand(this));
        CommandManager.registerCommand("store", new MessagesCommand(this));
        CommandManager.registerCommand("activity", new ActivityCommand(this));

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();

        // set the gamerules
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

    // get the class MariaDB
    public MariaDB getMariaDB() {
        return mariaDB;
    }

    // obtain luckperms api instance
    public static LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    public Scoreboard getPlayerListScoreboard() {
        return playerListScoreboard;
    }
}
