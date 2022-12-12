package net.trustgames.core.database.player_activity;

import java.sql.Timestamp;

public class PlayerActivity {

    /*
    This class is just used as getters and setters for the database player_activity
     */

    public PlayerActivity(int id, String uuid, String action, Timestamp time) {
        this.id = id;
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

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
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
