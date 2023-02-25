package net.trustgames.core.player.data;

import net.trustgames.core.Core;

public class PlayerStatsDB {

    private static final String tableName = "player_stats";
    private final Core core;

    public PlayerStatsDB(Core core) {
        this.core = core;
    }

    /**
     * use external method from MariaDB class
     * with specified SQL statement to create a new table
     * (is run async)
     */
    public void initializeTable() {
        String statement = "CREATE TABLE IF NOT EXISTS " + tableName + "(uuid VARCHAR(36) primary key, xp INT unsigned)";
        core.getMariaDB().initializeTable(tableName, statement);
    }
}
