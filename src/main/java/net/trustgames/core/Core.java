package net.trustgames.core;

import net.trustgames.core.announcer.ChatAnnouncer;
import net.trustgames.core.managers.ConfigManager;
import net.trustgames.core.managers.FolderManager;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.managers.CommandManager;
import net.trustgames.core.managers.EventsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Core extends JavaPlugin {

    FolderManager folderManager = new FolderManager(this);
    ConfigManager configManager = new ConfigManager(this);
    ChatAnnouncer chatAnnouncer = new ChatAnnouncer(this);
    EventsManager eventsManager = new EventsManager(this);
    CommandManager commandManager = new CommandManager(this);
    private MariaDB mariaDB;

    @Override
    public void onEnable() {

        /* TODO
        - Command completer (tab complete)
        - chat system
        - economy system
        - hotbar items
        - adminsystem (vanish, staffchat, ...)
        - report system
        - set gamerules (different for core lobby settings)
        - level system
        - cosmetics
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

        // connect to MariaDB database
        this.mariaDB = new MariaDB(this);
        mariaDB.initializeDatabase();

        // register events
        eventsManager.registerEvents();

        // register commands
        commandManager.registerAllCommands();

        // run ChatAnnouncer
        chatAnnouncer.announceMessages();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // close the HikariCP connection
        mariaDB.closeHikari();
    }

    // returns the MariaDB Database
    public MariaDB getMariaDB() {
        return mariaDB;
    }
}
