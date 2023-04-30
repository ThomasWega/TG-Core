package net.trustgames.core.config;

/**
 * The enum Cooldown value config.
 */
public enum CooldownValueConfig {
    /**
     * Small cooldown value config.
     */
    SMALL(1d),
    /**
     * Medium cooldown value config.
     */
    MEDIUM(5d),
    /**
     * Large cooldown value config.
     */
    LARGE(15d),
    /**
     * Warn messages limit sec cooldown value config.
     */
    WARN_MESSAGES_LIMIT_SEC(0.5d);

    /**
     * The Value.
     */
    public final double value;

    CooldownValueConfig(double value) {
        this.value = value;
    }
}
