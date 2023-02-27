package net.trustgames.core.commands.activity_commands;

import net.trustgames.core.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Query methods for activity commands.
 * Used to get the player's activity by uuid,
 * or get activity by id.
 */
public class ActivityFetcher {

    private final Core core;

    public ActivityFetcher(Core core) {
        this.core = core;
    }

    /**
     * @return ResultSet of all rows which matches
     */
    public ResultSet getActivityByUUID(String uuid) {
        try (Connection connection = core.getMariaDB().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_activity WHERE uuid = ? ORDER BY id DESC")) {
            statement.setString(1, uuid);
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param id Given ID in Base64 encoded or plain (decoded)
     * @return ResultSet of the row with the matching id
     */
    public ResultSet getActivityByID(String id) {

        // try to decode the id
        String decodedID = decodeID(id);

    /*
     if decode fails, it returns null.
     if it's null it's already in decoded format
    */
        if (decodedID == null)
            decodedID = id;

        try (Connection conn = core.getMariaDB().getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_activity WHERE id = ?")) {
            statement.setString(1, decodedID);
            try (ResultSet results = statement.executeQuery()) {
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param id Plain ID String
     * @return Encoded ID String
     */
    public String encodeId(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }

    /**
     * @param encodedId Encoded ID String
     * @return Decoded ID String or Null
     */
    public String decodeID(String encodedId) {
        try {
            return new String(Base64.getDecoder().decode(encodedId));
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
