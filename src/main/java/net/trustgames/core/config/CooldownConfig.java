package net.trustgames.core.config;

public enum CooldownConfig {
    SMALL(1d),
    MEDIUM(5d),
    LARGE(15d),
    WARN_MESSAGES_LIMIT_SEC(0.5d);

    public final double value;

    CooldownConfig(double value) {
        this.value = value;
    }
}
