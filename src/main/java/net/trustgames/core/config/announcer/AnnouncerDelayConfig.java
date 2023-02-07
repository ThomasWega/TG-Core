package net.trustgames.core.config.announcer;

public enum AnnouncerDelayConfig {
    FIRST(30L), // in seconds
    DELAY(360L); // in seconds

    private final long delay;

    AnnouncerDelayConfig(long delay) {
        this.delay = delay;
    }

    /**
     * @return Delay in seconds between announcing next message
     */
    public long getDelay() {
        return delay;
    }
}
