package net.trustgames.core.settings.cooldown;

public enum CoreCooldown {
    COMMAND_MAX_PER_SEC(5d),
    WARN_MESSAGES_LIMIT_SEC(0.5d);

    private final double value;

    CoreCooldown(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
