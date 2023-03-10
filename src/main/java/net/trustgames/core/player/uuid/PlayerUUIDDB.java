package net.trustgames.core.player.uuid;

import net.trustgames.core.Core;

public class PlayerUUIDDB {
    public static final String tableName = "player_uuid";
    private final Core core;

    public PlayerUUIDDB(Core core) {
        this.core = core;
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        String statement = "CREATE TABLE IF NOT EXISTS " + tableName + "(uuid VARCHAR(36) primary key, name VARCHAR(16))";
        core.getMariaDB().initializeTable(tableName, statement);
    }
}
