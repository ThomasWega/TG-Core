package net.trustgames.core.database.player_activity;

import java.sql.Timestamp;

public class PlayerActivity {

    /*
    This class is just used as getters and setters for the database player_activity
     */

    private final String uuid;
    private String action;
    private Timestamp time;

    public PlayerActivity(String uuid, String action, Timestamp time) {
        this.uuid = uuid;
        this.action = action;
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
