package net.trustgames.core.player.activity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * This class is just used as getters, setters and constructors
 * for the database player_activity.
 * Using lombok for this
 */
@Data
@AllArgsConstructor
public final class PlayerActivity {
    private final UUID uuid;
    private String ip;
    private String action;
    private Timestamp time;
}