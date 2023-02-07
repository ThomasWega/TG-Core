package net.trustgames.core.config.announcer;

public enum AnnouncerDelayConfig {
    DELAY(120L);

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
