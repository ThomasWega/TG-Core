package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
import net.trustgames.core.database.MariaDB;
import net.trustgames.core.debug.DebugColors;

import java.sql.*;

public class PlayerActivityDB {

    private final Core core;

    public PlayerActivityDB(Core core) {
        this.core = core;
    }

    public void initializePlayerActivityTable() {
        MariaDB mariaDB = new MariaDB(core);
        String preparedStatement = "CREATE TABLE IF NOT EXISTS player_activity(id INT primary key AUTO_INCREMENT, uuid varchar(36), action varchar(36), time DATETIME)";
        mariaDB.initializeDatabase("player_activity", preparedStatement);
    }

    // TODO by his action id too
    // find the correct players activity by his uuid
    public PlayerActivity findPlayerActivityByUUID(String uuid) {

        MariaDB mariaDB = new MariaDB(core);

        try {
            int id = getMaxID();
            core.getLogger().info(DebugColors.PURPLE_BACKGROUND + getMaxID());
            PreparedStatement statement = mariaDB.getConnection().prepareStatement("SELECT * FROM player_activity WHERE uuid = '0aee7bef-c0bb-3ae8-b18a-a525d6627e5b' ORDER BY id DESC LIMIT 1");
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();

            if (results.next()) {

                String action = results.getString("action");
                Timestamp time = results.getTimestamp("time");

                core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 5");
                core.getLogger().info(DebugColors.YELLOW_BACKGROUND + new PlayerActivity(id, uuid, action, time));
                return new PlayerActivity(id, uuid, action, time);
            }
            else{
                core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 6");
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // method that creates the player activity
    public void createPlayerActivity(PlayerActivity playerActivity) throws SQLException {

        MariaDB mariaDB = new MariaDB(core);

        // try to create PreparedStatement
        try (PreparedStatement statement = mariaDB.getConnection().prepareStatement("INSERT INTO player_activity(uuid, action, time) VALUES (?, ?, ?)")) {
            statement.setString(1, playerActivity.getUuid());
            statement.setString(2, playerActivity.getAction());
            statement.setTimestamp(3, playerActivity.getTime());

            core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 4");
            statement.executeUpdate();
        }
    }

    // method to update the player activity
    public void updatePlayerActivity(PlayerActivity playerActivity) throws SQLException {

        MariaDB mariaDB = new MariaDB(core);

        // try to create PreparedStatement
        try (PreparedStatement statement = mariaDB.getConnection().prepareStatement("UPDATE player_activity SET action = ?, time = ? WHERE uuid = ?")) {
        //    statement.setInt(1, playerActivity.getID());
            core.getLogger().info(DebugColors.PURPLE_BACKGROUND + playerActivity.getID());
            statement.setString(1, playerActivity.getAction());
            statement.setTimestamp(2, playerActivity.getTime());
            statement.setString(3, playerActivity.getUuid());

            core.getLogger().info(DebugColors.RED_BACKGROUND + "HERE 3");
            statement.executeUpdate();
        }
    }

    public int getMaxID(){

        MariaDB mariaDB = new MariaDB(core);

        int maxID = 0;

        try{
            Statement stmt = mariaDB.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM player_activity");

            while (rs.next()) {
                maxID = rs.getInt(1);
            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
        return maxID;
    }
}
