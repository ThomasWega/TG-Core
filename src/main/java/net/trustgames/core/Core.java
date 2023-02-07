package net.trustgames.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.commands.MessagesCommand;
import net.trustgames.core.commands.activity_commands.ActivityCommand;
import net.trustgames.core.commands.activity_commands.ActivityIdCommand;
import net.trustgames.core.database.MariaConfig;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.database.player_activity.PlayerActivityDB;
import net.trustgames.core.gamerules.CoreGamerules;
import net.trustgames.core.managers.*;
import net.trustgames.core.managers.chat.ChatDecoration;
import net.trustgames.core.managers.chat.ChatLimiter;
import net.trustgames.core.playerlist.PlayerListListener;
import net.trustgames.core.playerlist.PlayerListTeams;
import net.trustgames.core.config.command.MessagesCommandConfig;
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
    private final AnnounceManager announceManager = new AnnounceManager(this);
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
        - report system
        - level system
        - cosmetics (spawn particles, spawn sounds, balloons)
        - nick and skin changer
        - image maps
        - party and friends system
        - rotating heads
        - maintenance
        */

        /* SIDE ADDITIONS
        - hover on player name in chat, add info
         */

        /* CHANGE on server side
        - disallow some default command (/?, /version, /plugins, etc.) - make permissions false
        - change some default messages (unknown command, etc.) - change in server .yml files
         */

        // TODO register commands without plugin.yml
        // TODO test skin cache
        // TODO test uuid cache
        // TODO HOLO clickable
        // TODO use ProtocolLib everywhere
        // TODO NPC action - command prints the command in chat
        // TODO NPC add glow
        // TODO chat mention add who mentioned me
        // TODO Chat decoration use Component instead of String
        // TODO chat - first message is white name

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

        CoreGamerules.setGamerules();

        announceManager.announceMessages();
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
        pluginManager.registerEvents(new PlayerListListener(this), this);
        pluginManager.registerEvents(new ActivityCommand(this), this);
    }

    private void registerCommands() {

        // List of command to register
        HashMap<PluginCommand, CommandExecutor> cmdList = new HashMap<>();
        cmdList.put(getCommand("activity"), new ActivityCommand(this));
        cmdList.put(getCommand("activity-id"), new ActivityIdCommand(this));

        // Messages Commands
        for (MessagesCommandConfig msgCmd : MessagesCommandConfig.values()){
            cmdList.put(getCommand(msgCmd.name().toLowerCase()), new MessagesCommand());
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
        PlayerListTeams playerListTeams = new PlayerListTeams(this);
        playerListScoreboard = getServer().getScoreboardManager().getNewScoreboard();
        playerListTeams.createTeams();
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
