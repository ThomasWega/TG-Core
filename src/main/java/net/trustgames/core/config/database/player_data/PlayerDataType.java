package net.trustgames.core.config.database.player_data;

/**
 * Stores all the PlayerData types and their column names
 * which are saved in the player data table.
 */
public enum PlayerDataType {
    UUID("uuid", "VARCHAR(36) primary key"),
    PLAYER_NAME("name", "VARCHAR(16)"),
    KILLS("kills", "INT signed DEFAULT 0"),
    DEATHS("deaths", "INT DEFAULT 0"),
    GAMES_PLAYED("games_played", "INT DEFAULT 0"),
    PLAYTIME("playtime", "INT DEFAULT 0"),
    LEVEL_EXP("level_exp", "INT DEFAULT 0"),
    GOLD("gold", "INT DEFAULT 100"),
    RUBIES("rubies", "INT DEFAULT 0");

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
