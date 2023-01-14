package net.trustgames.core.database.player_activity;

import java.sql.Timestamp;

/**
    This class is just used as getters and setters for the database player_activity
     */
public class PlayerActivity {

    private final String uuid;
    private String ip;
    private String action;
    private Timestamp time;

    public PlayerActivity(String uuid, String ip, String action, Timestamp time) {
        this.uuid = uuid;
        this.ip = ip;
        this.action = action;
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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