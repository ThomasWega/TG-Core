package net.trustgames.core.player.activity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
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
    private List<Activity> activities;

    public void add(long id, String ip, String action, Timestamp time) {
        activities.add(new Activity(id, uuid, ip, action, time));
    }


    @AllArgsConstructor
    @Data
    public static class Activity {
        private long id;
        private UUID uuid;
        private String ip;
        private String action;
        private Timestamp time;

        public Activity(UUID uuid, String ip, String action, Timestamp time) {
            this.id = -1; // set the id to -1 since it's not known yet
            this.uuid = uuid;
            this.ip = ip;
            this.action = action;
            this.time = time;
        }
    }
}
