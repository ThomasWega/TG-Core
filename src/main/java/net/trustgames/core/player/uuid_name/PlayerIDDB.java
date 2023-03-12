package net.trustgames.core.player.uuid_name;

import net.trustgames.core.Core;

public class PlayerIDDB {
    public static final String tableName = "player_uuid";
    private final Core core;

    public PlayerIDDB(Core core) {
        this.core = core;
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        String statement = "CREATE TABLE IF NOT EXISTS " + tableName + "(uuid VARCHAR(36) primary key, name VARCHAR(16))";
        core.getDatabaseManager().initializeTable(tableName, statement);
    }
}
