package net.trustgames.core.config;

public enum CorePermissionsConfig {
    ADMIN("core.admin"),
    STAFF("core.staff"),
    TITAN("core.titan"),
    LORD("core.lord"),
    KNIGHT("core.knight"),
    PRIME("core.prime"),
    DEFAULT("core.default");

    public final String permission;

    CorePermissionsConfig(String permission) {
        this.permission = permission;
    }
}
