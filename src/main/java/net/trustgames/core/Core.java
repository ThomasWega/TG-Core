package net.trustgames.core;

import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.database.player_stats.PlayerStatsDB;
import net.trustgames.core.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Core extends JavaPlugin {

    FolderManager folderManager = new FolderManager(this);
    ConfigManager configManager = new ConfigManager(this);
    ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    EventsManager eventsManager = new EventsManager(this);
    CommandManager commandManager = new CommandManager(this);
    GameruleManager gameruleManager = new GameruleManager(this);
    // test
    PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    PlayerStatsDB playerStatsDB = new PlayerStatsDB(this);

    public MariaDB mariaDB = new MariaDB(this);

    @Override
    public void onEnable() {

        /* TODO
        - Command completer (tab complete)
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

        // create folders
        folderManager.createAllFolders();

        // create configs
        configManager.createAllConfigFiles();

        // create defaults for configs (including the default config.yml)
        configManager.createConfigsDefaults();

        // set gamerules
        gameruleManager.setGamerules();

        // connect to MariaDB database
//        this.mariaDB = new MariaDB(this);


        // register events
        eventsManager.registerEvents();

        // register commands
        commandManager.registerAllCommands();

        // run ChatAnnouncer
        chatAnnouncer.announceMessages();

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();
        // FIXME  playerStatsDB.initializePlayerStatsTable();


        // TODO custom head textures for items

        // FIXME too much mysql connections - close them!!!
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // close the HikariCP connection
        mariaDB.closeHikari();
    }

    public MariaDB getMariaDB(){
        return mariaDB;
    }
}
