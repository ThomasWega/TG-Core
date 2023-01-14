package net.trustgames.core.activity_command;

import net.trustgames.core.Core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class ActivityQuery {

    private final Core core;

    public ActivityQuery(Core core) {
        this.core = core;
    }

    public ResultSet getActivityByUUID(String uuid) {
        try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("SELECT * FROM player_activity WHERE uuid = ? ORDER BY id DESC")) {
            statement.setString(1, uuid);
            try (ResultSet results = statement.executeQuery()) {
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getActivityByID(String id){
        String decodedID = decodeID(id);

        if (decodedID == null)
            decodedID = id;

        try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("SELECT * FROM player_activity WHERE id = ?")) {
            statement.setString(1, decodedID);
            try (ResultSet results = statement.executeQuery()) {
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeId(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }

    public String decodeID(String encodedId) {
        try {
            return new String(Base64.getDecoder().decode(encodedId));
        } catch(IllegalArgumentException iae) {
            return null;
        }
    }
}
