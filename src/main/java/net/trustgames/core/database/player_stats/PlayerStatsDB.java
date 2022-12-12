package net.trustgames.core.database.player_stats;

import net.trustgames.core.Core;
import net.trustgames.core.database.MariaDB;

import java.sql.*;

public class PlayerStatsDB {

    private final Core core;

    public PlayerStatsDB(Core core) {
        this.core = core;
    }

    public void initializePlayerStatsTable(){
        MariaDB mariaDB = new MariaDB(core);
        String preparedStatement = "CREATE TABLE IF NOT EXISTS player_stats(uuid varchar(36) primary key, kills INT, deaths INT, games_played INT, playtime INT, level_exp DOUBLE, golds DOUBLE, rubies DOUBLE, last_join DATETIME)";
        mariaDB.initializeDatabase("player_stats", preparedStatement);
    }

    // find the correct players stats by his uuid
    public PlayerStats findPlayerStatsByUUID(String uuid) {

        MariaDB mariaDB = new MariaDB(core);

        try {
            PreparedStatement statement = mariaDB.getConnection().prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
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

        MariaDB mariaDB = new MariaDB(core);

        // try to create PreparedStatement
        try (PreparedStatement statement = mariaDB.getConnection().prepareStatement("INSERT INTO player_stats(uuid, kills, deaths, games_played, playtime, level_exp, golds, rubies, last_join) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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

        MariaDB mariaDB = new MariaDB(core);

        // try to create PreparedStatement
        try (PreparedStatement statement = mariaDB.getConnection().prepareStatement("UPDATE player_stats SET kills = ?, deaths = ?, games_played = ?, playtime = ?, level_exp = ?, golds = ?, rubies = ?, last_join = ? WHERE uuid = ?")) {
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
}
