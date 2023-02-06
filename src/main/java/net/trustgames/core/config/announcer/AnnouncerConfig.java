package net.trustgames.core.config.announcer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum AnnouncerConfig {
    DELAY(120L),
    MESSAGE_WEBSITE(
            "<newline>" +
                    "<yellow>1111" +
                    "<hover:show_text:'<green>CLICK TO OPEN'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL<reset></hover> non CLICK" +
                    "<yellow>1111" +
                    "<newline>"
    ),
    MESSAGE_STORE(
            "<newline>" +
                    "<yellow>1111" +
                    "<hover:show_text:'<green>CLICK TO OPEN'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL<reset></hover> non CLICK" +
                    "<yellow>1111" +
                    "<newline>"
    );

    private final Object value;

    AnnouncerConfig(Object value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public Component getMessage() {
        return MiniMessage.miniMessage().deserialize(value.toString());
    }

    /**
     * @return The delay between each announcement
     */
    public long getDelay(){
        return Long.parseLong(value.toString());
    }
}
