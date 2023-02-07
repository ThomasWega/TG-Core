package net.trustgames.core.config.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.utils.MiniMessageUtils;

public enum ChatConfig {
    PREFIX("<color:#00adc4>Chat | </color>"),
    COLOR("&f"),
    NAME_COLOR("&e"),
    ALLOW_COLORS_PERM("core.knight"),
    MENTION_COLOR("&a"),
    ON_COOLDOWN(PREFIX.value + "<dark_gray>Wait another <sec> seconds before using chat again!"),
    ON_SAME_COOLDOWN(PREFIX.value + "<dark_gray>Don't write the same message! (wait <sec> seconds)"),
    MENTION_ACTIONBAR("<gray>You've been mentioned in chat");

    private final String value;

    ChatConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public Component getText() {
        return MiniMessage.miniMessage().deserialize(value);
    }

    /**
     * @return String value of enum
     */
    public String getRaw(){
        return value;
    }

    /**
     * Replace sec tag with given seconds
     *
     * @param seconds Seconds to replace the tag with
     * @return New formatted Component with replaced sec tag
     */
    public Component addSeconds(double seconds){
        return MiniMessageUtils.addSeconds(seconds).deserialize(value);
    }
}