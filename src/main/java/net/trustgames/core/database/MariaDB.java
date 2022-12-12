package net.trustgames.core.database;

import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MariaDB {

    private final Core core;
    private Connection connection;

    public MariaDB(Core core) {
        this.core = core;
    }

    // method to check if table exists
    public static boolean tableExist(Connection connection, String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    rs.close();
                    break;
                }
            }
        }
        return tExists;
    }

    public Connection getConnection() {

        if (connection != null) {
            return connection;
        }

        // get the mariadb config credentials
        MariaConfig mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        String user = config.getString("mariadb.user");
        String password = config.getString("mariadb.password");
        String ip = config.getString("mariadb.ip");
        String port = config.getString("mariadb.port");
        String database = config.getString("mariadb.database");
        String url = "jdbc:mariadb://" + ip + ":" + port + "/" + database + "?user=" + user + "&password=" + password;

        // tries to connect to the database
            try {
                core.getLogger().info(DebugColors.CYAN + "Trying to connect to the database using HikariCP...");
                HikariDataSource hikari = getHikari();
                hikari.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
                hikari.addDataSourceProperty("url", url);
                connection = hikari.getConnection();
                core.getLogger().info(DebugColors.BLUE + "Successfully connected to the database using HikariCP");
                return connection;
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when connecting to the database using HikariCP");
                throw new RuntimeException(e);
            }
        }

    // checks if the table exists, if it doesn't, it creates one
    public void initializeDatabase(String tableName, String stringStatement) {
        if (isMySQLEnabled()){
            try {
                if (getConnection() != null) {
                    if (!tableExist(connection, tableName)) {
                        core.getLogger().info(DebugColors.CYAN + "Database table " + tableName + " doesn't exist, creating...");
                        PreparedStatement statement = getConnection().prepareStatement(stringStatement);
                        statement.executeUpdate();
                        statement.close();
                        if (tableExist(connection, tableName)) {
                            core.getLogger().info(DebugColors.BLUE + "Successfully created the table " + tableName);
                        }
                    }
                }
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Unable to create " + tableName + " table in the database!");
                throw new RuntimeException(e);
            }
        }
        else{
            core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "MySQL is turned off. Not connecting");
        }
    }

    // check if mysql is enabled in the config
    public boolean isMySQLEnabled() {
        MariaConfig mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        return Boolean.parseBoolean(config.getString("mariadb.enable"));
    }

    // close the hikari connection
    public void closeHikari() {
        if (isMySQLEnabled()) {
            HikariDataSource hikari = getHikari();
            if (hikari != null) {
                core.getLogger().info(DebugColors.CYAN + "Closing the HikariCP connection...");
                hikari.close();
                core.getLogger().info(DebugColors.BLUE + "Successfully closed the HikariCP connection");
            }
        }
    }

    // retrieve hikari
    public HikariDataSource getHikari() {
        return new HikariDataSource();
    }
}
