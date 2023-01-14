package net.trustgames.core.commands.activity_command;

import net.trustgames.core.Core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Query methods for activity commands.
 * Used to get the player's activity by uuid,
 * or get activity by id.
 */
public class ActivityQuery {

    private final Core core;

    public ActivityQuery(Core core) {
        this.core = core;
    }

    /**
     * Gets the all the player's activity by his uuid
     * with PreparedStatement and ResultSet.
     *
     * @param uuid Given UUID
     * @return ResultSet of all rows which matches
     */
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

    /**
     * Gets the activity by id with PreparedStatement.
     * It can also take both encoded id in Base64 or
     * plain decoded id in int.
     *
     * @param id Given ID
     * @return ResultSet of the row with the matching id
     */
    public ResultSet getActivityByID(String id){

        // try to decode the id
        String decodedID = decodeID(id);

        /*
         if decode fails, it returns null.
         if it's null it's already in decoded format
        */
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

    /**
     * Returns the encoded ID string of the given decoded ID string
     *
     * @param id Decoded ID String
     * @return Encoded ID String
     */
    public String encodeId(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }

    /**
     * Returns the decoded ID string of the given encoded ID String
     * or if the id cannot be decoded, it returns null.
     *
     * @param encodedId Encoded ID String
     * @return Decoded ID String or Null
     */
    public String decodeID(String encodedId) {
        try {
            return new String(Base64.getDecoder().decode(encodedId));
        } catch(IllegalArgumentException iae) {
            return null;
        }
    }
}
