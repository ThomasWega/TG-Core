package net.trustgames.core.config.player_data;

public enum PlayerDataUpdate {
    INTERVAL(5000); // in milliseconds


    private final long value;

    PlayerDataUpdate(long value) {
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
