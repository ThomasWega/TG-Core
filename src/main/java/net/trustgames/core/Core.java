package net.trustgames.core;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.trustgames.core.announcer.AnnouncerConfig;
import net.trustgames.core.announcer.ChatAnnouncer;
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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

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
    final GameruleManager gameruleManager = new GameruleManager();
    public CooldownManager cooldownManager = new CooldownManager(this);
    Scoreboard playerListScoreboard;
    public LuckPermsManager luckPermsManager;

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

        // TODO make HEX color ready
        // TODO register commands without plugin.yml

        // luckperms
        luckPermsManager = new LuckPermsManager(this);
        luckPermsManager.registerListeners();

        // create a data folder
        if (getDataFolder().mkdirs()) {
            getLogger().warning("Created config.yml");
        }
        //  FolderManager.createFolder(new File(getDataFolder() + File.separator + "data"));

        createConfigs();
        createConfigDefaults();

        registerEvents();
        registerCommands();

        playerList();

        // mariadb database
        playerActivityDB.initializePlayerActivityTable();

        // gamerules
        gameruleManager.setGamerules();

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


    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ActivityListener(this), this);
        pluginManager.registerEvents(new CommandManager(this), this);
        pluginManager.registerEvents(new CooldownManager(this), this);
        pluginManager.registerEvents(new ChatManager(this), this);
        pluginManager.registerEvents(new PlayerListListener(this), this);
        pluginManager.registerEvents(new ActivityCommand(this), this);

    }

    private void registerCommands() {
        MessagesConfig messagesConfig = new MessagesConfig(this);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(messagesConfig.getMessagesFile());

        // List of command to register
        HashMap<PluginCommand, CommandExecutor> cmdList = new HashMap<>();
        cmdList.put(getCommand("activity"), new ActivityCommand(this));
        cmdList.put(getCommand("activity-id"), new ActivityIdCommand(this));

        // Messages Commands
        String section = "messages";
        for (String s : Objects.requireNonNull(config.getConfigurationSection(section),
                "Configuration section " + section + " wasn't found in config!").getKeys(false)){
            cmdList.put(getCommand(s), new MessagesCommand(this));
        }

        for (PluginCommand cmd : cmdList.keySet()) {
            cmd.setExecutor(cmdList.get(cmd));
        }
    }

    private void createConfigs() {
        ConfigManager.createConfig(new File(getDataFolder(), "announcer.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "mariadb.yml"));
        ConfigManager.createConfig(new File(getDataFolder(), "commands.yml"));
    }

    private void createConfigDefaults() {
        DefaultConfig.create(getConfig());
        getConfig().options().copyDefaults(true);
        saveConfig();

        MariaConfig mariaConfig = new MariaConfig(this);
        mariaConfig.createDefaults();

        AnnouncerConfig announcerConfig = new AnnouncerConfig(this);
        announcerConfig.createDefaults();

        MessagesConfig messagesConfig = new MessagesConfig(this);
        messagesConfig.createDefaults();
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
}
