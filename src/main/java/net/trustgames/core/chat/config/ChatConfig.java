package net.trustgames.core.chat.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.MiniMessageUtils;

import java.util.UUID;

public enum ChatConfig {
    PREFIX("<color:#00adc4>Chat | </color>"),
    COLOR("&f"),
    NAME_COLOR("&e"),
    ALLOW_COLORS_PERM(CorePermissionsConfig.KNIGHT.permission),
    MENTION_COLOR("&a"),
    ON_COOLDOWN(PREFIX.value + "<dark_gray>Wait another <sec> seconds before using chat again!"),
    ON_SAME_COOLDOWN(PREFIX.value + "<dark_gray>Don't write the same message! (wait <sec> seconds)"),
    MENTION_ACTIONBAR("<gray><player_name> mentioned you");

    public final String value;

    ChatConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public final Component getText() {
        return MiniMessage.miniMessage().deserialize(value);
    }

    /**
     * Replace tags with player info
     *
     * @param uuid UUID of Player to replace the tags with info of
     * @return New formatted Component message with replaced tags
     */
    public final Component formatMessage(UUID uuid) {
        return MiniMessageUtils.format(uuid).deserialize(value);
    }

    /**
     * @return Color with value of enum
     */
    public final TextColor getColor() {
        return ColorUtils.color(value).color();
    }

    /**
     * Replace sec tag with given seconds
     *
     * @param seconds Seconds to replace the tag with
     * @return New formatted Component with replaced sec tag
     */
    public final Component addSeconds(double seconds) {
        return MiniMessageUtils.addSeconds(seconds).deserialize(value);
    }
}
