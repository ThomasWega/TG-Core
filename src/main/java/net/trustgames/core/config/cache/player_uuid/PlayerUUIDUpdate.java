package net.trustgames.core.config.cache.player_uuid;

public enum PlayerUUIDUpdate {
    INTERVAL(3600); // in seconds (1 hour)


    public final long value;

    PlayerUUIDUpdate(long value) {
        this.value = value;
    }
}
