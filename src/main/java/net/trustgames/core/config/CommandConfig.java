package net.trustgames.core.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.utils.MiniMessageUtils;

import java.util.UUID;

public enum CommandConfig {

    PREFIX("<color:#2472f0>Command | </color>"),
    PREFIX_DB("<color:#ed7168>Database | </color>"),
    MAX_PER_SEC(5d),
    COMMAND_NO_PERM(PREFIX.value + "<dark_gray>You don't have permission to perform this action!"),
    COMMAND_DATABASE_OFF(PREFIX_DB.value + "<dark_gray>Database is disabled!"),
    COMMAND_ONLY_PLAYER("This command can be executed by in-game players only!"),
    COMMAND_SPAM(PREFIX.value + "<dark_gray>Please don't spam the command!"),
    COMMAND_INVALID_ARG(PREFIX.value + "<dark_gray>You need to specify a valid argument!"),
    COMMAND_INVALID_PLAYER(PREFIX.value + "<dark_gray>The player <white><player> <dark_gray>doesn't exist!"),
    COMMAND_NO_PLAYER_ACT(PREFIX_DB.value + "<dark_gray>No activity data for player <white><player>"),
    COMMAND_NO_ID_ACT(PREFIX_DB.value + "<dark_gray>No activity data for ID <white><id>");

    public final Object value;

    CommandConfig(Object value) {
        this.value = value;
    }

    /**
     * @return double value of the enum
     */
    public double getDouble(){
        return ((double) value);
    }

    /**
     * @return Formatted component message
     */
    public Component getText() {
        return MiniMessage.miniMessage().deserialize(value.toString());
    }

    /**
     * Replace tags with player info
     *
     * @param uuid UUID of Player to replace the tags with info of
     * @return New formatted Component message with replaced tags
     */
    public Component formatMessage(UUID uuid) {
        return MiniMessageUtils.format(uuid).deserialize(value.toString());
    }

    /**
     * Replace id tag with given ID
     *
     * @param id ID to replace the tag with
     * @return New formatted Component with replaced id tag
     */
    public Component addID(String id){
        return MiniMessageUtils.addId(id).deserialize(value.toString());
    }
}
