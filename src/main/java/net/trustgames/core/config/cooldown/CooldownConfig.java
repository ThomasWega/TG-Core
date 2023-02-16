package net.trustgames.core.config.cooldown;

public enum CooldownConfig {
    SMALL(1d),
    MEDIUM(5d),
    LARGE(15d);

    public final double value;

    CooldownConfig(double value) {
        this.value = value;
    }
}
