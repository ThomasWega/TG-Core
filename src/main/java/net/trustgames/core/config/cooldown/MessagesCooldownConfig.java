package net.trustgames.core.config.cooldown;

public enum MessagesCooldownConfig {
    WARN_MESSAGES_LIMIT_SEC(0.5d);

    public final double value;

    MessagesCooldownConfig(double value) {
        this.value = value;
    }
}
