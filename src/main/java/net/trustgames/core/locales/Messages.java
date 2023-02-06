package net.trustgames.core.locales;

public class Messages {

    // placeholders
    public static final String IP = "play.trustgames.net";
    public static final String DISCORD = "discord.trustgames.net";
    public static final String WEBSITE = "www.trustgames.net";
    public static final String STORE = "store.trustgames.net";

    // prefixes
    public static final String PREFIX_CHAT = "&3Chat | ";
    public static final String PREFIX_DATABASE = "&#ed7168Database | ";

    // messages
    public static final String RESTART = "&eServer is restarting...";
    public static final String NO_PERM = PREFIX_CHAT + "&8You don't have permission to perform this action!";
    public static final String DATABASE_OFF = PREFIX_DATABASE + "&8Database is disabled!";
    public static final String COMMAND_ONLY_PLAYER = "This command can be executed by in-game players only!";
    public static final String COMMAND_SPAM = PREFIX_CHAT + "&8Please don't spam the command!";
    public static final String COMMAND_INVALID_ARG = PREFIX_CHAT + "&8You need to specify a valid argument!";
    public static final String COMMAND_INVALID_PLAYER = PREFIX_CHAT + "&8The player %s doesn't exist!";
    public static final String COMMAND_NO_PLAYER_ACT = PREFIX_DATABASE + "&8No activity data for player %s!";
    public static final String COMMAND_NO_ID_ACT = PREFIX_DATABASE + "&8No activity data for ID %s!";
    public static final String CHAT_ON_COOLDOWN = PREFIX_CHAT + "&8Wait another %s seconds before using chat again!";
    public static final String CHAT_ON_SAME_COOLDOWN = PREFIX_CHAT + "&8Don't write the same message! (wait %s seconds)";
    public static final String CHAT_MENTION_ACTIONBAR = "&7You have been mentioned in chat";

    // cooldowns
    public static final double COMMAND_MAX_PER_SEC = 5d;
    public static final double WARN_MESSAGES_LIMIT_SEC = 0.5d;
    public static final double CHAT_LIMIT_SEC_DEFAULT = 15d;
    public static final double CHAT_LIMIT_SEC_VIP1 = 10d;
    public static final double CHAT_LIMIT_SEC_VIP2 = 5d;
    public static final double CHAT_LIMIT_SEC_VIP3 = 3d;
    public static final double CHAT_LIMIT_SEC_VIP4 = 0.1d;
    public static final double CHAT_LIMIT_SAME_SEC_DEFAULT = 120d;
    public static final double CHAT_LIMIT_SAME_SEC_VIP1 = 60d;
    public static final double CHAT_LIMIT_SAME_SEC_VIP2 = 45d;
    public static final double CHAT_LIMIT_SAME_SEC_VIP3 = 25d;
    public static final double CHAT_LIMIT_SAME_SEC_VIP4 = 10d;

    // tablist
    public static final String[] TABLIST_HEADER = {"&e&lTRUSTGAMES &f- &7Chillin' on the hub"};
    public static final String[] TABLIST_FOOTER = {"&astore.trustgames.net"};

    // chat
    public static final String CHAT_COLOR = "&f";
    public static final String CHAT_NAME_COLOR = "&e";
    public static final String CHAT_ALLOW_COLORS_PERM = "core.vip2";
    public static final String CHAT_MENTION_COLOR = "&a";
}
