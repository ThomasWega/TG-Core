package net.trustgames.core.settings.server;

public enum CoreServer {
    RESTART("&eServer is restarting...");

    private final String value;

    CoreServer(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
