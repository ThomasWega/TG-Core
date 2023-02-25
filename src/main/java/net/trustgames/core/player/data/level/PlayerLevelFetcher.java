package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.IntConsumer;

public class PlayerLevelFetcher {

    private final Core core;

    public PlayerLevelFetcher(Core core) {
        this.core = core;
    }

    private static final String tableName = "player_stats";

    // TODO make async

    public void fetch(UUID uuid, IntConsumer callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement("SELECT xp FROM " + tableName + " WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        int xp = results.getInt("xp");
                        callback.accept(xp);
                        return;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            callback.accept(0);
        });
    }


    public void update(UUID uuid, int xp) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (PreparedStatement statement = core.getMariaDB().getConnection().prepareStatement(
                    "INSERT INTO " + tableName + "(uuid, xp) VALUES (?, ?) ON DUPLICATE KEY UPDATE xp = VALUES(xp)")) {
                statement.setString(1, uuid.toString());
                statement.setInt(2, xp);

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
