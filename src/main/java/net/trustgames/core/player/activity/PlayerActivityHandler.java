package net.trustgames.core.player.activity;

import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is used for listeners for table player_activity
 */
public class PlayerActivityHandler implements Listener {

    private final Core core;

    public PlayerActivityHandler(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = EntityCache.getUUID(event.getPlayer());

        write(uuid, "JOIN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")");
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = EntityCache.getUUID(event.getPlayer());

        write(uuid, "QUIT SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")");
    }

    /**
     * Gets the player's last activity by using external method from PlayerActivityDB
     * to find player's last activity by his uuid. If the activity is null, meaning the player
     * probably doesn't have any activities saved in the table yet, it creates one with specified values.
     * Calls the provided callback function with the resulting PlayerActivity object.
     *
     * @param uuid     UUID of Player to write activity to
     * @param callback Callback function to be called with the resulting PlayerActivity object
     */
    private void get(UUID uuid, Consumer<PlayerActivity> callback) {
        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            callback.accept(null);
            return;
        }

        playerActivityDB.fetchByUUID(uuid, playerActivity -> {
            if (playerActivity == null) {
                playerActivity = new PlayerActivity(uuid, Objects.requireNonNull(
                                player.getAddress(), "Player " + uuid + " IP address is null!")
                        .getHostString(), "FIRST JOIN PORT " + Bukkit.getServer().getPort(),
                        new Timestamp(Instant.now().toEpochMilli()));

                playerActivityDB.add(playerActivity);
                callback.accept(null);
            } else {
                callback.accept(playerActivity);
            }
        });
    }

    /**
     * writes the values for the newly created player activity to the new PlayerActivity instance.
     * Then it creates the full row.
     *
     * @param uuid   UUID of Player to write activity to
     * @param action What actions to write
     */
    private void write(UUID uuid, String action) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;
        if (core.getMariaDB().isMySQLDisabled()) return;

        get(uuid, playerActivity -> {
            if (playerActivity != null) {
                PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

                playerActivity.setIp(Objects.requireNonNull(player.getAddress()).getHostString());

                playerActivity.setAction(action);
                playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));

                playerActivityDB.add(playerActivity);
            }
        });
    }
}
