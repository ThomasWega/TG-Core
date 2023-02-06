package net.trustgames.core.settings.command;

public enum CoreCommand {

    PREFIX("&#2472f0Command | "),
    PREFIX_DB("&#ed7168Database | "),
    COMMAND_NO_PERM(PREFIX.value + "&8You don't have permission to perform this action!"),
    COMMAND_DATABASE_OFF(PREFIX_DB.value + "&8Database is disabled!"),
    COMMAND_ONLY_PLAYER("This command can be executed by in-game players only!"),
    COMMAND_SPAM(PREFIX.value + "&8Please don't spam the command!"),
    COMMAND_INVALID_ARG(PREFIX.value + "&8You need to specify a valid argument!"),
    COMMAND_INVALID_PLAYER(PREFIX.value + "&8The player %s doesn't exist!"),
    COMMAND_NO_PLAYER_ACT(PREFIX_DB.value + "&8No activity data for player %s!"),
    COMMAND_NO_ID_ACT(PREFIX_DB.value + "&8No activity data for ID %s!");

    private final String value;

    CoreCommand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
