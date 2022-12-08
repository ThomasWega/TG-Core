package net.trustgames.core.database;

import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import net.trustgames.core.database.models.PlayerStats;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

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

    // checks if the player_stats table exists, if it doesn't, it creates one
    public void initializeDatabase() {
        if (isMySQLEnabled()){
            try {
                if (getConnection() != null) {
                    if (!tableExist(connection, "player_stats")) {
                        core.getLogger().info(DebugColors.CYAN + "Database table player_stats doesn't exist, creating...");
                        PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS player_stats(uuid varchar(36) primary key, kills INT, deaths INT, games_played INT, playtime INT, level_exp DOUBLE, golds DOUBLE, rubies DOUBLE, last_join DATETIME)");
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
        else{
            core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "MySQL is turned off. Not connecting");
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
                    int games_played = results.getInt("games_played");
                    int playtime = results.getInt("playtime");
                    double level_exp = results.getDouble("level_exp");
                    double golds = results.getDouble("golds");
                    double rubies = results.getDouble("rubies");
                    Timestamp last_join = results.getTimestamp("last_join");

                    return new PlayerStats(uuid, kills, deaths, games_played, playtime, level_exp, golds, rubies, last_join);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        return null;
    }

    // method that creates the player stats
    public void createPlayerStats(PlayerStats playerStats) throws SQLException {
            // try to create PreparedStatement
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO player_stats(uuid, kills, deaths, games_played, playtime, level_exp, golds, rubies, last_join) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, playerStats.getUuid());
                statement.setInt(2, playerStats.getKills());
                statement.setInt(3, playerStats.getDeaths());
                statement.setInt(4, playerStats.getGames_played());
                statement.setInt(5, playerStats.getPlaytime());
                statement.setDouble(6, playerStats.getLevel_exp());
                statement.setDouble(7, playerStats.getGolds());
                statement.setDouble(8, playerStats.getRubies());
                statement.setTimestamp(9, playerStats.getLast_join());

                statement.executeUpdate();
        }
    }

    // method to update the player stats
    public void updatePlayerStats(PlayerStats playerStats) throws SQLException {
            // try to create PreparedStatement
            try (PreparedStatement statement = getConnection().prepareStatement("UPDATE player_stats SET kills = ?, deaths = ?, games_played = ?, playtime = ?, level_exp = ?, golds = ?, rubies = ?, last_join = ? WHERE uuid = ?")) {
                statement.setInt(1, playerStats.getKills());
                statement.setInt(2, playerStats.getDeaths());
                statement.setInt(3, playerStats.getGames_played());
                statement.setDouble(4, playerStats.getPlaytime());
                statement.setDouble(5, playerStats.getLevel_exp());
                statement.setDouble(6, playerStats.getGolds());
                statement.setDouble(7, playerStats.getRubies());
                statement.setTimestamp(8, playerStats.getLast_join());
                statement.setString(9, playerStats.getUuid());

                statement.executeUpdate();
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
