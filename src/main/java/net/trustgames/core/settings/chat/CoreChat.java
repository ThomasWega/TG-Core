package net.trustgames.core.settings.chat;

public enum CoreChat {
    PREFIX("&3Chat | "),
    CHAT_COLOR("&f"),
    CHAT_NAME_COLOR("&e"),
    CHAT_ALLOW_COLORS_PERM("core.knight"),
    CHAT_MENTION_COLOR("&a"),
    CHAT_ON_COOLDOWN(PREFIX.value + "&8Wait another %s seconds before using chat again!"),
    CHAT_ON_SAME_COOLDOWN(PREFIX.value + "&8Don't write the same message! (wait %s seconds)"),
    CHAT_MENTION_ACTIONBAR("&7You have been mentioned in chat");

    private final String value;

    CoreChat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
