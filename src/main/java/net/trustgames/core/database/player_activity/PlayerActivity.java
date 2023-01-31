package net.trustgames.core.database.player_activity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

/**
 * This class is just used as getters, setters and constructors
 * for the database player_activity.
 * Using lombok for this
 */
@Data
@AllArgsConstructor
public class PlayerActivity {
    private final String uuid;
    private String ip;
    private String action;
    private Timestamp time;
}