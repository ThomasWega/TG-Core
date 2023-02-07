package net.trustgames.core.config.announcer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum AnnouncerMessagesConfig {
    MESSAGE_WEBSITE(
            "<newline>" +
                    "<yellow>1111" +
                    "<hover:show_text:'<green>CLICK TO OPEN'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL<reset> non CLICK" +
                    "<yellow>1111" +
                    "<newline>"
    ),
    MESSAGE_STORE(
            "<newline>" +
                    "<yellow>1111" +
                    "<hover:show_text:'<green>CLICK TO OPEN'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL<reset> non CLICK" +
                    "<yellow>1111" +
                    "<newline>"
    );

    private final String value;

    AnnouncerMessagesConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public Component getMessage() {
        return MiniMessage.miniMessage().deserialize(value);
    }
}
