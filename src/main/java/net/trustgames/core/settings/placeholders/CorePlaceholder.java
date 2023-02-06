package net.trustgames.core.settings.placeholders;

public enum CorePlaceholder {
    IP("play.trustgames.net"),
    WEBSITE("discord.trustgames.net"),
    STORE("store.trustgames.net"),
    DISCORD("discord.trustgames.net");

    private final String value;

    CorePlaceholder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
