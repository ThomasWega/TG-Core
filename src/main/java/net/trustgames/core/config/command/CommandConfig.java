package net.trustgames.core.config.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.utils.MiniMessageUtils;
import org.bukkit.entity.Player;

public enum CommandConfig {

    PREFIX("<color:#2472f0>Command | </color>"),
    PREFIX_DB("<color:#ed7168>Database | </color>"),
    COMMAND_NO_PERM(PREFIX.value + "<dark_gray>You don't have permission to perform this action!"),
    COMMAND_DATABASE_OFF(PREFIX_DB.value + "<dark_gray>Database is disabled!"),
    COMMAND_ONLY_PLAYER("This command can be executed by in-game players only!"),
    COMMAND_SPAM(PREFIX.value + "<dark_gray>Please don't spam the command!"),
    COMMAND_INVALID_ARG(PREFIX.value + "<dark_gray>You need to specify a valid argument!"),
    COMMAND_INVALID_PLAYER(PREFIX.value + "<dark_gray>The player <white><player> <dark_gray>doesn't exist!"),
    COMMAND_NO_PLAYER_ACT(PREFIX_DB.value + "<dark_gray>No activity data for player <white><player>"),
    COMMAND_NO_ID_ACT(PREFIX_DB.value + "<dark_gray>No activity data for ID <white><id>");

    private final String value;

    CommandConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public Component getText() {
        return MiniMessage.miniMessage().deserialize(value);
    }

    /**
     * @return String value of enum
     */
    public String getRaw(){
        return value;
    }

    /**
     * Replace tags with player info
     *
     * @param player Player to replace the tags with info of
     * @return New formatted Component message with replaced tags
     */
    public Component formatMessage(Player player) {
        return MiniMessageUtils.format(player).deserialize(value);
    }

    /**
     * Replace id tag with given ID
     *
     * @param id ID to replace the tag with
     * @return New formatted Component with replaced id tag
     */
    public Component addID(String id){
        return MiniMessageUtils.addId(id).deserialize(value);
    }
}
