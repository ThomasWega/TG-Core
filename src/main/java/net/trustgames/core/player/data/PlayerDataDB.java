package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataTypes;

/**
 * This class handles the creation of the data database table
 */
public class PlayerDataDB {

    public static final String tableName = "player_data";
    private final Core core;

    public PlayerDataDB(Core core) {
        this.core = core;
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

        for (PlayerDataTypes dataType : PlayerDataTypes.values()) {
            statement.append(dataType.getColumnName())
                    .append(" ")
                    .append(dataType.getColumnType())
                    .append(",");

        }
        statement.deleteCharAt(statement.length() - 1);
        statement.append(")");

        core.getMariaDB().initializeTable(tableName, statement.toString());
    }
}
