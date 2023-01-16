package net.trustgames.core.database;

import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

/**
 * This class handles the basic MariaDB and HikariCP methods such as getting connection,
 * creating the database, table (if not exists) and closing the hikari connection. Note that the
 * plugin#getLogger is used instead of Bukkit#getLogger, because async methods should not access Bukkit API
 */
public class MariaDB {

    private final Core core;
    public HikariDataSource hikariDataSource;
    private Connection connection;

    public MariaDB(Core core) {
        this.core = core;
    }

    private MariaConfig mariaConfig;

    /**
     * Check if table exists
     *
     * @param connection HikariCP connection
     * @param tableName The name of the table
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
     create the specified database if it doesn't exist yet
     (is run async)
    */
    private void createDatabaseIfNotExists() {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {

            YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
            String user = config.getString("mariadb.user");
            String password = config.getString("mariadb.password");
            String ip = config.getString("mariadb.ip");
            String port = config.getString("mariadb.port");
            String database = config.getString("mariadb.database");

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                try (Connection connection = DriverManager.getConnection("jdbc:mariadb://" + ip + ":" + port + "/", user, password); PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database)) {
                    statement.executeUpdate();
                }
            } catch (SQLException | ClassNotFoundException e) {
                core.getLogger().severe("Unable to create " + database + " database!");
                throw new RuntimeException(e);
            }
        });
    }

    /**
     gets the connection. Checks if the connection isn't null. If it isn't, it will return connection
     if the connection is null, meaning it probably doesn't exist, it will create a new connection and return it
    */
    public Connection getConnection() {

        if (connection != null) {
            return connection;

        } else {

            YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
            String user = config.getString("mariadb.user");
            String password = config.getString("mariadb.password");
            String ip = config.getString("mariadb.ip");
            String port = config.getString("mariadb.port");
            String database = config.getString("mariadb.database");

            try {
                hikariDataSource = new HikariDataSource();
                HikariDataSource ds = hikariDataSource;
                ds.setDriverClassName("org.mariadb.jdbc.Driver");
                ds.setJdbcUrl("jdbc:mariadb://" + ip + ":" + port + "/" + database);
                ds.addDataSourceProperty("user", user);
                ds.addDataSourceProperty("password", password);
                ds.setMaximumPoolSize(5);
                ds.setPoolName("HikariCP-Core");

                connection = ds.getConnection();
                return connection;
            } catch (SQLException e) {
                core.getLogger().severe("ERROR: Connecting to the database using HikariCP");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     checks if the table exists, if it doesn't, it creates one using the given SQL statement
     (is run async)
     * @param tableName The name of the table
     * @param stringStatement The SQL statement in String
    */
    public void initializeTable(String tableName, String stringStatement) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            if (isMySQLDisabled()) {
                core.getLogger().warning("MySQL is turned off. Not initializing table " + tableName);
                return;
            }
            createDatabaseIfNotExists();

            core.getServer().getScheduler().runTaskLaterAsynchronously(core, () -> {
                try {
                    if (getConnection() == null) return;
                    if (tableExist(getConnection(), tableName)) return;
                    core.getLogger().info("Database table " + tableName + " doesn't exist, creating...");
                    try (PreparedStatement statement = getConnection().prepareStatement(stringStatement)) {
                        statement.executeUpdate();
                        if (tableExist(getConnection(), tableName)) {
                            core.getLogger().warning("Successfully created the table " + tableName);
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
        mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        return !Boolean.parseBoolean(config.getString("mariadb.enable"));
    }

    public void closeHikari() {
        if (isMySQLDisabled()) return;
        hikariDataSource.close();
    }
}
