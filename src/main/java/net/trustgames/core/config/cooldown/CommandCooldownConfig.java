package net.trustgames.core.config.cooldown;

public enum CommandCooldownConfig {
    MAX_PER_SEC(5d);

    public final double value;

    CommandCooldownConfig(double value) {
        this.value = value;
    }
}
