package net.trustgames.core.chat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum TextCommandsConfig {
    WEBSITE("<newline>" +
            "<color:#5757cf>You can visit our website by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://www.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    ),
    STORE("<newline>" +
            "<color:#cf9117>You can check benefits on our store by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://discord.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    ),
    DISCORD("<newline>" +
            "<color:#99a3ff>You can join our discord server by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://discord.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    );

    private final String value;

    TextCommandsConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component message
     */
    public final Component getValue() {
        return MiniMessage.miniMessage().deserialize(value);
    }
}
