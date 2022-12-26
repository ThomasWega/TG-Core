package net.trustgames.core;

import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.chat.MessageLimiter;
import net.trustgames.core.commands.MessagesCommand;
import net.trustgames.core.commands.MessagesConfig;
import net.trustgames.core.config.DefaultConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Core extends JavaPlugin {

    MariaDB mariaDB = new MariaDB(this);
    ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    ServerShutdownManager serverShutdownManager = new ServerShutdownManager(this);
    GameruleManager gameruleManager = new GameruleManager(this);

    @Override
    public void onEnable() {

        /* ADD
        - Command completer (tab complete)
        - configurable extandable .yml text info commands (/discord, /store, /help)
        - chat system
        - economy system
        - hotbar items
        - adminsystem (vanish, menus, ...)
        - report system
        - level system
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer
        - holo system
        - image maps
        - party and friends system
        - rotating heads
        - npc system
        */

        // create a folder
        FolderManager.createDataFolder(getDataFolder());
        //  FolderManager.createFolder(new File(getDataFolder() + File.separator + "data"));

        // create config files
        ConfigManager.createConfig(new File(getDataFolder(), "announcer.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "mariadb.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "messages.yml"));

        // create config defaults
        DefaultConfig.create(getConfig()); getConfig().options().copyDefaults(true); saveConfig();
        MariaConfig mariaConfig = new MariaConfig(this);
        mariaConfig.createDefaults();
        AnnouncerConfig announcerConfig = new AnnouncerConfig(this);
        announcerConfig.createDefaults();
        MessagesConfig messagesConfig = new MessagesConfig(this);
        messagesConfig.createDefaults();

        // register events
        EventManager.registerEvent(new ActivityListener(this), this);
        EventManager.registerEvent(new CommandManager(this), this);
        EventManager.registerEvent(new MessageLimiter(this), this);

        // register commands
        CommandManager.registerCommand("discord", new MessagesCommand(this));
        CommandManager.registerCommand("website", new MessagesCommand(this));
        CommandManager.registerCommand("store", new MessagesCommand(this));

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
}
