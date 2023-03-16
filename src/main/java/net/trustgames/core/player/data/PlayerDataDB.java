package net.trustgames.core.player.data;

import net.trustgames.core.managers.database.DatabaseManager;
import net.trustgames.core.player.data.config.PlayerDataType;

/**
 * This class handles the creation of the data database table
 */
public final class PlayerDataDB {

    public static final String tableName = "player_data";
    private final DatabaseManager databaseManager;

    public PlayerDataDB(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        initializeTable();
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        StringBuilder statement = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(");

        for (PlayerDataType dataType : PlayerDataType.values()) {
            if (dataType != PlayerDataType.LEVEL) {
                statement.append(dataType.getColumnName())
                        .append(" ")
                        .append(dataType.getColumnType())
                        .append(",");
            }

        }
        statement.deleteCharAt(statement.length() - 1);
        statement.append(")");

        databaseManager.initializeTable(tableName, statement.toString());
    }
}
