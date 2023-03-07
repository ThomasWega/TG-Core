package net.trustgames.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

/**
 * This class handles the basic MariaDB and HikariCP methods such as getting connection,
 * creating the database, table (if not exists) and closing the hikari connection. Note that the
 * plugin#getLogger is used instead of Bukkit#getLogger, because async methods should not access Bukkit API
 */
public final class MariaDB {

    private final Core core;
    public HikariDataSource dataSource;
    private MariaConfig mariaConfig;

    public MariaDB(Core core) {
        this.core = core;
    }

    /**
     * Check if table exists.
     *
     * @implNote The connection isn't closed by this method
     *
     * @param connection HikariCP connection
     * @param tableName  The name of the table
     * @return if the table already exists
     * @throws SQLException if it can't get the ResultSet
     */
    private static boolean tableExist(Connection connection, String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    /**
     * gets a new connection from the hikaricp pool
     * */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            core.getLogger().severe("Getting a new connection from HikariCP");
            throw new RuntimeException(e);
        }
    }

    public void initializePool() {
        mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        String user = config.getString("mariadb.user");
        String password = config.getString("mariadb.password");
        String ip = config.getString("mariadb.ip");
        String port = config.getString("mariadb.port");
        String database = config.getString("mariadb.database");
        int poolSize = config.getInt("hikaricp.pool-size");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mariadb://" + ip + ":" + port + "/" + database);
        hikariConfig.addDataSourceProperty("user", user);
        hikariConfig.addDataSourceProperty("password", password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("HikariCP-Core");

        dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * checks if the table exists, if it doesn't, it creates one using the given SQL statement
     * (is run async)
     *
     * @param tableName       The name of the table
     * @param stringStatement The SQL statement in String
     */
    public void initializeTable(String tableName, String stringStatement) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            if (isMySQLDisabled()) {
                core.getLogger().warning("MySQL is turned off. Not initializing table " + tableName);
                return;
            }

            core.getServer().getScheduler().runTaskLaterAsynchronously(core, () -> {
                try (Connection connection = getConnection()) {
                    if (tableExist(connection, tableName)) return;
                    core.getLogger().info("Database table " + tableName + " doesn't exist, creating...");
                    try (PreparedStatement statement = connection.prepareStatement(stringStatement)) {
                        statement.executeUpdate();
                        if (tableExist(connection, tableName)) {
                            core.getLogger().finest("Successfully created the table " + tableName);
                        }
                    }
                } catch (SQLException e) {
                    core.getLogger().severe("Unable to create " + tableName + " table in the database!");
                    throw new RuntimeException(e);
                }
            }, YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile()).getLong("delay.database-table-creation"));
        });
    }

    /**
     * @return true if mysql is disabled
     */
    public boolean isMySQLDisabled() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        return !Boolean.parseBoolean(config.getString("mariadb.enable"));
    }

    public void closeHikari() {
        if (isMySQLDisabled()) return;
        dataSource.close();
    }
}
