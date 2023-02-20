package net.trustgames.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.commands.messages_commands.MessagesCommands;
import net.trustgames.core.commands.activity_commands.ActivityCommand;
import net.trustgames.core.commands.activity_commands.ActivityIdCommand;
import net.trustgames.core.commands.messages_commands.MessagesCommandsConfig;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.player_activity.ActivityListener;
import net.trustgames.core.player_activity.PlayerActivityDB;
import net.trustgames.core.protection.CoreGamerulesHandler;
import net.trustgames.core.announcer.AnnounceHandler;
import net.trustgames.core.managers.*;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.ChatLimiter;
import net.trustgames.core.player_list.PlayerListHandler;
import net.trustgames.core.player_list.PlayerListTeams;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.HashMap;

/**
 * Main class of the Core plugin, which registers all the events and commands.
 * Handles the plugin enable and disable.
 * Has methods to get other instances of other classes and initializes other classes
 * to be able to access them from external plugins
 */
public final class Core extends JavaPlugin {

    final MariaDB mariaDB = new MariaDB(this);
    private final AnnounceHandler announceHandler = new AnnounceHandler(this);
    private final PlayerActivityDB playerActivityDB = new PlayerActivityDB(this);
    private final ShutdownManager shutdownManager = new ShutdownManager(this);
    public CooldownManager cooldownManager = new CooldownManager();
    private Scoreboard playerListScoreboard;
    public LuckPermsManager luckPermsManager;

    private ProtocolManager protocolManager;


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
        // TODO NPC protocolib

        // luckperms
        luckPermsManager = new LuckPermsManager(this);
        luckPermsManager.registerListeners();

        // protocollib
        protocolManager = ProtocolLibrary.getProtocolManager();

        // create a data folder
        if (getDataFolder().mkdirs()) {
            getLogger().warning("Created main plugin folder");
        }
        //  FolderManager.createFolder(new File(getDataFolder() + File.separator + "data"));

        createConfigs();
        createConfigsDefaults();

        registerEvents();
        registerCommands();

        playerList();

        playerActivityDB.initializePlayerActivityTable();

        CoreGamerulesHandler.setGamerules();

        announceHandler.announceMessages();
    }

    @Override
    public void onDisable() {

        // run the server shutdown manager (kick players, write activity, ...)
        shutdownManager.kickPlayers();

        mariaDB.closeHikari();
    }


    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ActivityListener(this), this);
        pluginManager.registerEvents(new CommandManager(), this);
        pluginManager.registerEvents(new CooldownManager(), this);
        pluginManager.registerEvents(new PlayerManager(), this);
        pluginManager.registerEvents(new ChatLimiter(), this);
        pluginManager.registerEvents(new ChatDecoration(), this);
        pluginManager.registerEvents(new PlayerListHandler(this), this);
        pluginManager.registerEvents(new ActivityCommand(this), this);
    }

    private void registerCommands() {

        // List of command to register
        HashMap<PluginCommand, CommandExecutor> cmdList = new HashMap<>();
        cmdList.put(getCommand("activity"), new ActivityCommand(this));
        cmdList.put(getCommand("activity-id"), new ActivityIdCommand(this));

        // Messages Commands
        for (MessagesCommandsConfig msgCmd : MessagesCommandsConfig.values()){
            cmdList.put(getCommand(msgCmd.name().toLowerCase()), new MessagesCommands());
        }

        for (PluginCommand cmd : cmdList.keySet()) {
            cmd.setExecutor(cmdList.get(cmd));
        }
    }

    private void createConfigs() {
        ConfigManager.createConfig(new File(getDataFolder(), "mariadb.yml"));
    }

    private void createConfigsDefaults() {
        MariaConfig mariaConfig = new MariaConfig(this);
        mariaConfig.createDefaults();
    }

    public MariaDB getMariaDB() {
        return mariaDB;
    }


    public static LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    /**
     * Create the playlist and create teams for it
     * with luckperms groups weight support
     */
    private void playerList(){
        PlayerListTeams playerListTeamsManager = new PlayerListTeams(this);
        playerListScoreboard = getServer().getScoreboardManager().getNewScoreboard();
        playerListTeamsManager.createTeams();
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

    public ProtocolManager getProtocolManager(){
        return protocolManager;
    }

}
