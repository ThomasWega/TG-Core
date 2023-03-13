package net.trustgames.core.config.player_data;

public enum PlayerDataConfig {

    // in milliseconds
    UPDATE_INTERVAL(5000),
    DATA_EXPIRY(1800000);



    private final long value;

    PlayerDataConfig(long value) {
        this.value = value;
    }

    /**
     * @return Converted milliseconds to ticks
     */
    public final long getTicks() {
        return value / 50;
    }

    public final long getSeconds(){
        return value / 1000;
    }
}
