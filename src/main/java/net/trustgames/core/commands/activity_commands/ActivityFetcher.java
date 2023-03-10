package net.trustgames.core.commands.activity_commands;

import net.trustgames.core.Core;

import java.sql.*;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Query methods for activity commands.
 * Used to get the player's activity by uuid,
 * or get activity by id.
 */
public final class ActivityFetcher {

    private final Core core;

    public ActivityFetcher(Core core) {
        this.core = core;
    }

    /**
     * Returns the ResultSet of all rows which matches in an async callback
     * @param uuid UUID of the player to get the Activity for
     * @param callback Callback where the result will be saved
     * @see ActivityFetcher#fetchActivityByID(String, Consumer)
     */
    public void fetchActivityByUUID(UUID uuid, Consumer<ResultSet> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Connection connection = core.getMariaDB().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_activity WHERE uuid = ? ORDER BY id DESC")) {
                statement.setString(1, uuid.toString());
                ResultSet results = statement.executeQuery();
                callback.accept(results);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Returns the ResultSet of the row with the matching id in async callback
     *
     * @param id Given ID in Base64 encoded or plain (decoded)
     * @param callback Callback where the result will be saved
     * @see ActivityFetcher#fetchActivityByUUID(UUID, Consumer)
     */
    public void fetchActivityByID(String id, Consumer<ResultSet> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
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
                    callback.accept(results);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
