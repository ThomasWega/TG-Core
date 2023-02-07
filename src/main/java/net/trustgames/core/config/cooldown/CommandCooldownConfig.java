package net.trustgames.core.config.cooldown;

public enum CommandCooldownConfig {
    MAX_PER_SEC(5d);

    private final double value;

    CommandCooldownConfig(double value) {
        this.value = value;
    }

    /**
     * @return Value of the enum
     */
    public double getValue() {
        return value;
    }
}
