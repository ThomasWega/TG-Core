package net.trustgames.core.player.display_name;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.toolkit.config.chat.ChatConfig;

public enum PlayerDisplayNameConfig {
    DISPLAY_NAME(ChatConfig.NAME_COLOR.getValue() +
            "<hover:show_text:'TO ADD'>" +
            "<click:suggest_command:'<player_name>'>" +
            "<player_name>" +
            "</click>" +
            "</hover>");

    private final String value;

    PlayerDisplayNameConfig(String value) {
        this.value = value;
    }

    public Component getDisplayName(Audience audience) {
        return MiniMessage.miniMessage().deserialize(
                value, MiniPlaceholders.getAudiencePlaceholders(audience)
        );
    }
}
