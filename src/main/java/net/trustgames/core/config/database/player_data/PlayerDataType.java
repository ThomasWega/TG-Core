package net.trustgames.core.config.database.player_data;

/**
 * Stores all the PlayerData types and their column names
 * which are saved in the player data table.
 */
public enum PlayerDataType {
    UUID("uuid", "VARCHAR(36) primary key"),
    PLAYER_NAME("name", "VARCHAR(16)"),
    KILLS("kills", "INT signed"),
    DEATHS("deaths", "INT signed"),
    GAMES_PLAYED("games_played", "INT signed"),
    PLAYTIME("playtime", "INT signed"),
    LEVEL_EXP("level_exp", "INT signed"),
    GOLD("gold", "INT signed"),
    RUBIES("rubies", "INT signed");

    private final String columnName;
    private final String columnType;

    PlayerDataType(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }
}
