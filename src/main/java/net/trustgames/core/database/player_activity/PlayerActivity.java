package net.trustgames.core.database.player_activity;

import java.sql.Timestamp;

public class PlayerActivity {

    /*
    This class is just used as getters and setters for the database player_activity
     */

    public PlayerActivity(String uuid, String action, Timestamp time) {
        this.uuid = uuid;
        this.action = action;
        this.time = time;
    }

    private String uuid;
    private int id;
    private String action;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    private Timestamp time;
}
