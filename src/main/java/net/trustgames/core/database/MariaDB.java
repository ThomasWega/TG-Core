package net.trustgames.core.database;

import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import net.trustgames.core.models.PlayerStats;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

public class MariaDB {

    private final Core core;
    private Connection connection;

    public MariaDB(Core core) {
        this.core = core;
    }

    public Connection getConnection() {

        if (connection != null) {
            return connection;
        }

        core.getLogger().info(DebugColors.CYAN + "Trying to connect to the database using HikariCP...");

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

    // checks if the player_stats table exists, if it doesn't, it creates one
    public void initializeDatabase() {
        try {
            if (getConnection() != null) {
                if (!tableExist(connection, "player_stats")) {
                    core.getLogger().info(DebugColors.CYAN + "Database table player_stats doesn't exist, creating...");
                    PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS player_stats(uuid varchar(36) primary key, kills INT, deaths INT, coins DOUBLE)");
                    statement.executeUpdate();
                    statement.close();
                    if (tableExist(connection, "player_stats")) {
                        core.getLogger().info(DebugColors.BLUE + "Successfully created the table player_stats");
                    }
                }
            }
        } catch (SQLException e) {
            core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Unable to create player_stats table in the database!");
            throw new RuntimeException(e);
        }
    }

    // find the correct players stats by his uuid
    public PlayerStats findPlayerStatsByUUID(String uuid) {

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            statement.close();

            if (results.next()) {

                int kills = results.getInt("kills");
                int deaths = results.getInt("deaths");
                double coins = results.getInt("coins");

                return new PlayerStats(uuid, kills, deaths, coins);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // method that creates the player stats
    public void createPlayerStats(PlayerStats playerStats) throws SQLException {

        // try to create PreparedStatement
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO player_stats(uuid, kills, deaths, coins) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, playerStats.getUuid());
            statement.setInt(2, playerStats.getKills());
            statement.setInt(3, playerStats.getDeaths());
            statement.setDouble(4, playerStats.getCoins());

            statement.executeUpdate();
        }
    }

    // method to update the player stats
    public void updatePlayerStats(PlayerStats playerStats) throws SQLException {

        // try to create PreparedStatement
        try (PreparedStatement statement = getConnection().prepareStatement("UPDATE player_stats SET kills = ?, deaths = ?, coins = ? WHERE uuid = ?")) {
            statement.setInt(1, playerStats.getKills());
            statement.setInt(2, playerStats.getDeaths());
            statement.setDouble(3, playerStats.getCoins());
            statement.setString(4, playerStats.getUuid());

            statement.executeUpdate();
        }
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

    public void closeHikari(){
        HikariDataSource hikari = getHikari();
        if (hikari != null){
            core.getLogger().info(DebugColors.CYAN + "Closing the HikariCP connection...");
            hikari.close();
            core.getLogger().info(DebugColors.BLUE + "Successfully closed the HikariCP connection");
        }
    }

    // retrieve hikari
    public HikariDataSource getHikari(){
        return new HikariDataSource();
    }
}
