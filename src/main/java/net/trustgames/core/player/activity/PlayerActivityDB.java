package net.trustgames.core.player.activity;

import net.trustgames.core.managers.database.DatabaseManager;

/**
 * This class is used to handle the database table for player activity
 * including its methods, functions etc.
 */
public final class PlayerActivityDB {

    public static final String tableName = "player_activity";

    private final DatabaseManager databaseManager;

    public PlayerActivityDB(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        initializeTable();
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        String statement = "CREATE TABLE IF NOT EXISTS " + tableName + "(id BIGINT unsigned primary key AUTO_INCREMENT, uuid VARCHAR(36), ip VARCHAR(15), action TINYTEXT, time DATETIME)";
        databaseManager.initializeTable(tableName, statement);
    }
}
