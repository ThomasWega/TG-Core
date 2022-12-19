package net.trustgames.core;

import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.config.DefaultConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.inventories.HotbarListeners;
import net.trustgames.core.managers.*;
import net.trustgames.core.spawn.SetSpawnCommand;
import net.trustgames.core.spawn.Spawn;
import net.trustgames.core.spawn.SpawnCommand;
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

        // register commands
        CommandManager.registerCommand("spawn", new SpawnCommand(this));
        CommandManager.registerCommand("setspawn", new SetSpawnCommand(this));

        // create config files
        ConfigManager.createConfig(new File(getDataFolder(), "spawn.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "announcer.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "mariadb.yml"));

        // create config defaults
        DefaultConfig.create(getConfig()); getConfig().options().copyDefaults(true); saveConfig();
        MariaConfig mariaConfig = new MariaConfig(this);
        mariaConfig.createDefaults();
        AnnouncerConfig announcerConfig = new AnnouncerConfig(this);
        announcerConfig.createDefaults();

        // create a folder
        FolderManager.createDataFolder(getDataFolder());
      //  FolderManager.createFolder(new File(getDataFolder() + File.separator + "data"));

        // register events
        EventsManager.registerEvent(new Spawn(this), this);
        EventsManager.registerEvent(new ActivityListener(this), this);
        EventsManager.registerEvent(new HotbarListeners(), this);

        // run ChatAnnouncer
        chatAnnouncer.announceMessages();

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();

        // set the gamerules
        gameruleManager.setGamerules("world");

        // API TEST
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
