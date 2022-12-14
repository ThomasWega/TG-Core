package net.trustgames.core;

import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Core extends JavaPlugin {

    public MariaDB mariaDB = new MariaDB(this);
    FolderManager folderManager = new FolderManager(this);
    ConfigManager configManager = new ConfigManager(this);
    ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    EventsManager eventsManager = new EventsManager(this);
    CommandManager commandManager = new CommandManager(this);
    GameruleManager gameruleManager = new GameruleManager(this);
    PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    ServerShutdownManager serverShutdownManager = new ServerShutdownManager(this);

    @Override
    public void onEnable() {

        /* ADD
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

        // register events
        eventsManager.registerEvents();

        // register commands
        commandManager.registerAllCommands();

        // run ChatAnnouncer
        chatAnnouncer.announceMessages();

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

/* TODO
    Save data on database on server shutdown
    because on server shutdown, the plugin is first disabled and after that the player is kicked
    meaning the onPlayerQuit method won't save the player's quit
     */

        // run the server shutdown manager (kick players, ...)
        serverShutdownManager.kickPlayers();

        // close the HikariCP connection
        mariaDB.closeHikari();
    }


    // get the class MariaDB
    public MariaDB getMariaDB() {
        return mariaDB;
    }
}
